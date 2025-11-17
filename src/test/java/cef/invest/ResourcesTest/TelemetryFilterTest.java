package cef.invest.ResourcesTest;

import cef.financial.domain.service.TelemetryService;
import cef.financial.infra.TelemetryFilter;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryFilterTest {

    @Mock
    TelemetryService telemetryService;

    @Mock
    ContainerRequestContext requestContext;

    @Mock
    ContainerResponseContext responseContext;

    @Mock
    UriInfo uriInfo;

    TelemetryFilter filter;

    @BeforeEach
    void setup() {
        filter = new TelemetryFilter();
        filter.telemetryService = telemetryService;
    }

    @Test
    @DisplayName("1. Deve salvar o timestamp de início na requisição")
    void testRequestFilter_SalvaStartTime() {
        filter.filter(requestContext);

        verify(requestContext, times(1))
                .setProperty(eq("telemetryStartTime"), anyLong());
    }

    @Test
    @DisplayName("2. Deve calcular duração e chamar telemetryService.record() corretamente")
    void testResponseFilter_ChamaRecord() {
        long fakeStart = System.currentTimeMillis() - 150;

        when(requestContext.getProperty("telemetryStartTime")).thenReturn(fakeStart);
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("investimentos");

        filter.filter(requestContext, responseContext);

        verify(telemetryService, times(1))
                .record(eq("GET investimentos"), anyLong());
    }

    @Test
    @DisplayName("3. Não deve chamar telemetryService se startTime for nulo")
    void testResponseFilter_StartNull() {
        when(requestContext.getProperty("telemetryStartTime")).thenReturn(null);

        filter.filter(requestContext, responseContext);

        verify(telemetryService, never()).record(anyString(), anyLong());
    }

    @Test
    @DisplayName("4. Não deve chamar telemetryService se startTime for inválido")
    void testResponseFilter_StartTimeInvalido() {
        when(requestContext.getProperty("telemetryStartTime")).thenReturn("string");

        filter.filter(requestContext, responseContext);

        verify(telemetryService, never()).record(anyString(), anyLong());
    }

    @Test
    @DisplayName("5. Deve montar o serviceName corretamente: METHOD + PATH")
    void testResponseFilter_MontaServiceNameCorreto() {
        long start = System.currentTimeMillis() - 50;

        when(requestContext.getProperty("telemetryStartTime")).thenReturn(start);
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("telemetria/dia");

        filter.filter(requestContext, responseContext);

        verify(telemetryService)
                .record(eq("POST telemetria/dia"), anyLong());
    }
}
