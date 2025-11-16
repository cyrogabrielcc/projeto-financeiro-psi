package cef.financial.api.resources;

import cef.financial.domain.dto.RiskProfileResponseDTO;
import cef.financial.domain.service.RiskProfileService;
import cef.financial.domain.service.TelemetryService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/perfil-risco")
//@Authenticated
//@SecurityRequirement(name = "bearerAuth")
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
