package cef.financial.api.resources;

import cef.financial.domain.dto.RiskProfileResponseDTO;
import cef.financial.domain.service.RiskProfileService;
import cef.financial.domain.service.TelemetryService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Path("/perfil-risco")
@Produces(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Perfil de Risco", description = "Cálculo do perfil de risco de um cliente com base em dados financeiros e comportamentais")
public class RiskProfileResource {

    @Inject
    RiskProfileService riskProfileService;

    @Inject
    TelemetryService telemetryService;

    @GET
    @Path("/{clienteId}")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Calcular perfil de risco do cliente",
            description = "Retorna o perfil de risco do cliente (ex.: CONSERVADOR, MODERADO, AGRESSIVO) com base em seu histórico e características financeiras."
    )
    @APIResponse(
            responseCode = "200",
            description = "Perfil de risco calculado com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RiskProfileResponseDTO.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Cliente não encontrado"
    )
    @APIResponse(
            responseCode = "403",
            description = "Acesso negado — usuário não possui permissão"
    )
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
