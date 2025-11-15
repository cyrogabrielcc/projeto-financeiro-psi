package cef.financial.api;

import cef.financial.domain.dto.TelemetryResponse;
import cef.financial.domain.service.TelemetryService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDate;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class TelemetryResource {

    @Inject
    TelemetryService telemetryService;

    @GET
    @Path("/telemetria")
    @RolesAllowed({"admin"})
    public TelemetryResponse telemetria(@QueryParam("inicio") String inicio,
                                        @QueryParam("fim") String fim) {
        LocalDate from = inicio != null ? LocalDate.parse(inicio) : null;
        LocalDate to = fim != null ? LocalDate.parse(fim) : null;
        return telemetryService.getTelemetry(from, to);
    }
}
