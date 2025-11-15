package cef.financial.api;

import cef.financial.domain.dto.RiskProfileResponseDTO;
import cef.financial.domain.service.RiskProfileService;
import cef.financial.domain.service.TelemetryService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@Path("/perfil-risco")
@Authenticated
@SecurityRequirement(name = "bearerAuth")
//@Consumes(MediaType.APPLICATION_JSON)
//@Produces(MediaType.APPLICATION_JSON)
public class RiskProfileResource {

    @Inject
    RiskProfileService riskProfileService;

    @Inject
    TelemetryService telemetryService;

    @GET
    @Path("/{clienteId}")
    @RolesAllowed({"user", "admin"})
    public RiskProfileResponseDTO perfilRisco(@PathParam("clienteId") Long clienteId) {
        long start = System.currentTimeMillis();
        try {
            return riskProfileService.calculateProfile(clienteId);
        } finally {
            long duration = System.currentTimeMillis() - start;
            telemetryService.record("perfil-risco", duration);
        }
    }
}
