package cef.financial.api.resources;

import cef.financial.domain.dto.TelemetryResponseDTO;
import cef.financial.domain.service.TelemetryService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.Content;

import java.time.LocalDate;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Telemetria", description = "Consulta estatísticas de performance dos endpoints")
public class TelemetryResource {

    @Inject
    TelemetryService telemetryService;

    @GET
    @Path("/telemetria")
    @RolesAllowed({"admin"})
    @Operation(
            summary = "Consultar telemetria",
            description = "Retorna métricas agregadas de uso e performance dos endpoints, como quantidade de chamadas e tempo de processamento."
    )
    @APIResponse(
            responseCode = "200",
            description = "Telemetria retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TelemetryResponseDTO.class)
            )
    )
    @APIResponse(
            responseCode = "403",
            description = "Acesso negado — apenas administradores podem consultar telemetria"
    )
    public TelemetryResponseDTO telemetria(
            @Parameter(description = "Data inicial no formato yyyy-MM-dd", example = "2025-11-01")
            @QueryParam("inicio") String inicio,
            @Parameter(description = "Data final no formato yyyy-MM-dd", example = "2025-11-30")
            @QueryParam("fim") String fim) {

        LocalDate from = inicio != null ? LocalDate.parse(inicio) : null;
        LocalDate to = fim != null ? LocalDate.parse(fim) : null;
        return telemetryService.getTelemetry(from, to);
    }
}
