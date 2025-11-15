package cef.financial.api;

import cef.financial.domain.dto.RiskProfileResponse;
import cef.financial.domain.service.RiskProfileService;
import cef.financial.domain.service.TelemetryService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class RiskProfileResource {

    @Inject
    RiskProfileService riskProfileService;

    @Inject
    TelemetryService telemetryService;

    // 5. GET /perfil-risco/{clienteId}
    @GET
    @Path("/perfil-risco/{clienteId}")
    @RolesAllowed({"user", "admin"})
    public RiskProfileResponse perfilRisco(@PathParam("clienteId") Long clienteId) {
        long start = System.currentTimeMillis();
        try {
            return riskProfileService.calculateProfile(clienteId);
        } finally {
            long duration = System.currentTimeMillis() - start;
            telemetryService.record("perfil-risco", duration);
        }
    }
}
