package cef.financial.infra;

import cef.financial.domain.service.TelemetryService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER)
public class TelemetryFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME_PROPERTY = "telemetryStartTime";

    @Inject
    TelemetryService telemetryService;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Marca o início da requisição
        requestContext.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object startObj = requestContext.getProperty(START_TIME_PROPERTY);
        if (!(startObj instanceof Long)) {
            return;
        }

        long startTime = (Long) startObj;
        long durationMs = System.currentTimeMillis() - startTime;

        // Monta um "nome de serviço" legível: ex. "GET /investimentos", "POST /simulacoes"
        String httpMethod = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String serviceName = httpMethod + " " + path;

        // Registra o evento de telemetria
        telemetryService.record(serviceName, durationMs);
    }
}
