package cef.invest.resources.test.ResourcesTest; // Ajuste para o seu pacote

import cef.financial.api.resources.TelemetryResource;
import cef.financial.domain.dto.TelemetryResponseDTO;
import cef.financial.domain.model.TelemetryEvent;
import cef.financial.domain.service.TelemetryService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelemetryResourceTest {

    @Mock
    TelemetryService telemetryService;

    @InjectMocks
    TelemetryResource telemetryResource;

    @Captor
    ArgumentCaptor<LocalDate> dateCaptor;

    @Test
    @DisplayName("Deve ter anotações de classe corretas")
    void testClassAnnotations() {
        // Verifica as anotações de classe
        assertTrue(TelemetryResource.class.isAnnotationPresent(Path.class));
        assertEquals("/", TelemetryResource.class.getAnnotation(Path.class).value());

        assertTrue(TelemetryResource.class.isAnnotationPresent(Consumes.class));
        assertEquals(MediaType.APPLICATION_JSON, TelemetryResource.class.getAnnotation(Consumes.class).value()[0]);

        assertTrue(TelemetryResource.class.isAnnotationPresent(Produces.class));
        assertEquals(MediaType.APPLICATION_JSON, TelemetryResource.class.getAnnotation(Produces.class).value()[0]);

        assertTrue(TelemetryResource.class.isAnnotationPresent(Authenticated.class));
    }

    @Test
    @DisplayName("Método telemetria deve ter anotações corretas")
    void testTelemetriaMethodAnnotations() throws NoSuchMethodException {
        // Os parâmetros são dois 'String'
        var method = TelemetryResource.class.getMethod("telemetria", String.class, String.class);

        // Verifica anotações do método
        assertTrue(method.isAnnotationPresent(GET.class));
        assertTrue(method.isAnnotationPresent(RolesAllowed.class));
        assertTrue(method.isAnnotationPresent(Path.class));

        RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
        assertArrayEquals(new String[]{"admin"}, rolesAnnotation.value());
        assertEquals("/telemetria", method.getAnnotation(Path.class).value());
    }

    @Test
    @DisplayName("Deve chamar o serviço com as datas parseadas corretamente")
    void testTelemetriaLogicWithAllParams() {
        // Arrange
        String inicioStr = "2025-01-01";
        String fimStr = "2025-01-31";
        LocalDate inicioDate = LocalDate.of(2025, 1, 1);
        LocalDate fimDate = LocalDate.of(2025, 1, 31);

        TelemetryResponseDTO mockResponse = new TelemetryResponseDTO(); // DTO de resposta mockado
        when(telemetryService.getTelemetry(inicioDate, fimDate)).thenReturn(mockResponse);

        // Act
        TelemetryResponseDTO actualResponse = telemetryResource.telemetria(inicioStr, fimStr);

        // Assert
        assertNotNull(actualResponse);
        assertSame(mockResponse, actualResponse, "A resposta deve ser o mesmo objeto retornado pelo serviço");
        verify(telemetryService).getTelemetry(inicioDate, fimDate); // Verifica se o serviço foi chamado com as datas corretas
    }

    @Test
    @DisplayName("Deve chamar o serviço com null quando as datas não são fornecidas")
    void testTelemetriaLogicWithNullParams() {
        // Arrange
        TelemetryResponseDTO mockResponse = new TelemetryResponseDTO();
        when(telemetryService.getTelemetry(null, null)).thenReturn(mockResponse);

        // Act
        TelemetryResponseDTO actualResponse = telemetryResource.telemetria(null, null);

        // Assert
        assertNotNull(actualResponse);
        assertSame(mockResponse, actualResponse);
        verify(telemetryService).getTelemetry(null, null); // Verifica a chamada com nulos
    }

    @Test
    @DisplayName("Deve chamar o serviço com uma data e um null")
    void testTelemetriaLogicWithOneParam() {
        // Arrange
        String inicioStr = "2025-02-10";
        LocalDate inicioDate = LocalDate.of(2025, 2, 10);
        TelemetryResponseDTO mockResponse = new TelemetryResponseDTO();
        when(telemetryService.getTelemetry(inicioDate, null)).thenReturn(mockResponse);

        // Act
        TelemetryResponseDTO actualResponse = telemetryResource.telemetria(inicioStr, null);

        // Assert
        assertSame(mockResponse, actualResponse);
        verify(telemetryService).getTelemetry(inicioDate, null); // Verifica a chamada com uma data e um nulo
    }

    @Test
    @DisplayName("Deve lançar DateTimeParseException se o formato da data for inválido")
    void testTelemetriaWithInvalidDateFormat() {

        String inicioStr = "data-invalida";
        String fimStr = "2025-01-31";

              assertThrows(DateTimeParseException.class, () -> {
            telemetryResource.telemetria(inicioStr, fimStr);
        }, "Uma data inválida deve lançar DateTimeParseException");
    }

    @Test
    @DisplayName("Deve instanciar DTOs e Modelos para cobertura")
    void testDTOAndModelInstantiability() {
        // Garante que as classes são instanciáveis (aumenta cobertura)
        assertNotNull(new TelemetryResponseDTO());
        assertNotNull(new TelemetryEvent());

        // Testa DTOs internos
        TelemetryResponseDTO.ServiceMetric metric = new TelemetryResponseDTO.ServiceMetric();
        metric.nome = "test";
        metric.quantidadeChamadas = 100;
        metric.mediaTempoRespostaMs = 50.5;
        assertEquals("test", metric.nome);
        assertEquals(100, metric.quantidadeChamadas);
        assertEquals(50.5, metric.mediaTempoRespostaMs);

        TelemetryResponseDTO.Periodo periodo = new TelemetryResponseDTO.Periodo();
        periodo.inicio = LocalDate.now();
        periodo.fim = LocalDate.now().plusDays(1);
        assertNotNull(periodo.inicio);
        assertNotNull(periodo.fim);
    }
}