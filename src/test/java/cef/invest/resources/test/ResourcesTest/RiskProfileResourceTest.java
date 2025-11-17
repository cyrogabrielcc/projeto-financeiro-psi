package cef.invest.resources.test.ResourcesTest;

import cef.financial.api.resources.RiskProfileResource;
import cef.financial.domain.dto.RiskProfileResponseDTO;
import cef.financial.domain.service.RiskProfileService;
import cef.financial.domain.service.TelemetryService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RiskProfileResource com 100% de cobertura
 */
@ExtendWith(MockitoExtension.class)
class RiskProfileResourceTest {

    @Mock
    private RiskProfileService riskProfileService;

    @Mock
    private TelemetryService telemetryService;

    @InjectMocks
    private RiskProfileResource riskProfileResource;

    @Test
    void testClassAnnotations() {
        // Verifica as anotações de classe
        assertTrue(RiskProfileResource.class.isAnnotationPresent(Path.class));
        assertEquals("/perfil-risco", RiskProfileResource.class.getAnnotation(Path.class).value());
    }

    @Test
    void testFieldAnnotations() throws NoSuchFieldException {
        // Verifica as anotações dos campos
        var riskProfileField = RiskProfileResource.class.getDeclaredField("riskProfileService");
        assertTrue(riskProfileField.isAnnotationPresent(Inject.class));

        var telemetryField = RiskProfileResource.class.getDeclaredField("telemetryService");
        assertTrue(telemetryField.isAnnotationPresent(Inject.class));
    }

    @Test
    void testMethodAnnotations() throws NoSuchMethodException {
        var method = RiskProfileResource.class.getMethod("perfilRisco", Long.class);

        // Verifica anotações do método
        assertTrue(method.isAnnotationPresent(GET.class));
        assertTrue(method.isAnnotationPresent(RolesAllowed.class));
        assertTrue(method.isAnnotationPresent(Path.class));

        RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
        assertArrayEquals(new String[]{"user", "admin"}, rolesAnnotation.value());
        assertEquals("/{clienteId}", method.getAnnotation(Path.class).value());
    }

    @Test
    void perfilRisco_deveRetornarPerfilCalculado() {
        // Arrange
        Long clienteId = 1L;
        RiskProfileResponseDTO perfilEsperado = new RiskProfileResponseDTO();
        perfilEsperado.perfil = "MODERADO";
        perfilEsperado.pontuacao = 75;

        when(riskProfileService.calculateProfile(clienteId)).thenReturn(perfilEsperado);

        // Act
        RiskProfileResponseDTO result = riskProfileResource.perfilRisco(clienteId);

        // Assert
        assertNotNull(result);
        assertEquals("MODERADO", result.perfil);
        assertEquals(75, result.pontuacao);
        verify(riskProfileService).calculateProfile(clienteId);
        verify(telemetryService).record(eq("perfil-risco"), anyLong());
    }

    @Test
    void perfilRisco_deveFuncionarComClienteIdNulo() {
        // Arrange
        RiskProfileResponseDTO perfilEsperado = new RiskProfileResponseDTO();
        perfilEsperado.perfil = "INDEFINIDO";
        perfilEsperado.pontuacao = 0;

        when(riskProfileService.calculateProfile(null)).thenReturn(perfilEsperado);

        // Act
        RiskProfileResponseDTO result = riskProfileResource.perfilRisco(null);

        // Assert
        assertNotNull(result);
        assertEquals("INDEFINIDO", result.perfil);
        assertEquals(0, result.pontuacao);
        verify(riskProfileService).calculateProfile(null);
        verify(telemetryService).record(eq("perfil-risco"), anyLong());
    }

    @Test
    void perfilRisco_deveFuncionarComClienteIdZero() {
        // Arrange
        Long clienteId = 0L;
        RiskProfileResponseDTO perfilEsperado = new RiskProfileResponseDTO();
        perfilEsperado.perfil = "CONSERVADOR";
        perfilEsperado.pontuacao = 25;

        when(riskProfileService.calculateProfile(clienteId)).thenReturn(perfilEsperado);

        // Act
        RiskProfileResponseDTO result = riskProfileResource.perfilRisco(clienteId);

        // Assert
        assertNotNull(result);
        assertEquals("CONSERVADOR", result.perfil);
        assertEquals(25, result.pontuacao);
        verify(riskProfileService).calculateProfile(clienteId);
        verify(telemetryService).record(eq("perfil-risco"), anyLong());
    }

    @Test
    void perfilRisco_deveFuncionarComClienteIdNegativo() {
        // Arrange
        Long clienteId = -1L;
        RiskProfileResponseDTO perfilEsperado = new RiskProfileResponseDTO();
        perfilEsperado.perfil = "ARROJADO";
        perfilEsperado.pontuacao = 90;

        when(riskProfileService.calculateProfile(clienteId)).thenReturn(perfilEsperado);

        // Act
        RiskProfileResponseDTO result = riskProfileResource.perfilRisco(clienteId);

        // Assert
        assertNotNull(result);
        assertEquals("ARROJADO", result.perfil);
        assertEquals(90, result.pontuacao);
        verify(riskProfileService).calculateProfile(clienteId);
        verify(telemetryService).record(eq("perfil-risco"), anyLong());
    }

    @Test
    void perfilRisco_deveChamarTelemetryMesmoComExcecao() {
        // Arrange
        Long clienteId = 1L;
        when(riskProfileService.calculateProfile(clienteId)).thenThrow(new RuntimeException("Erro no serviço"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            riskProfileResource.perfilRisco(clienteId);
        });

        // Assert - Telemetry deve ser chamado mesmo com exceção
        verify(telemetryService).record(eq("perfil-risco"), anyLong());
    }

    @Test
    void perfilRisco_deveRegistrarTempoDeExecucao() {
        // Arrange
        Long clienteId = 1L;
        RiskProfileResponseDTO perfilEsperado = new RiskProfileResponseDTO();
        perfilEsperado.perfil = "MODERADO";
        perfilEsperado.pontuacao = 65;

        when(riskProfileService.calculateProfile(clienteId)).thenReturn(perfilEsperado);

        // Act
        RiskProfileResponseDTO result = riskProfileResource.perfilRisco(clienteId);

        // Assert
        assertNotNull(result);
        verify(riskProfileService).calculateProfile(clienteId);
        verify(telemetryService).record(eq("perfil-risco"), anyLong());
    }

    @Test
    void perfilRisco_deveFuncionarComDiferentesTiposDePerfil() {
        // Arrange
        Long clienteId = 1L;

        String[] perfis = {"CONSERVADOR", "MODERADO", "ARROJADO", "INDEFINIDO"};
        int[] scores = {30, 65, 85, 0};

        for (int i = 0; i < perfis.length; i++) {
            RiskProfileResponseDTO perfilEsperado = new RiskProfileResponseDTO();
            perfilEsperado.perfil = perfis[i];
            perfilEsperado.pontuacao = scores[i];

            when(riskProfileService.calculateProfile(clienteId)).thenReturn(perfilEsperado);

            // Act
            RiskProfileResponseDTO result = riskProfileResource.perfilRisco(clienteId);

            // Assert
            assertNotNull(result);
            assertEquals(perfis[i], result.perfil);
            assertEquals(scores[i], result.pontuacao);
        }

        verify(riskProfileService, times(perfis.length)).calculateProfile(clienteId);
        verify(telemetryService, times(perfis.length)).record(eq("perfil-risco"), anyLong());
    }

    @Test
    void perfilRisco_deveChamarServicosApenasUmaVez() {
        // Arrange
        Long clienteId = 1L;
        RiskProfileResponseDTO perfilEsperado = new RiskProfileResponseDTO();
        perfilEsperado.perfil = "MODERADO";
        perfilEsperado.pontuacao = 70;

        when(riskProfileService.calculateProfile(clienteId)).thenReturn(perfilEsperado);

        // Act
        riskProfileResource.perfilRisco(clienteId);

        // Assert
        verify(riskProfileService, times(1)).calculateProfile(clienteId);
        verify(telemetryService, times(1)).record(eq("perfil-risco"), anyLong());
        verifyNoMoreInteractions(riskProfileService, telemetryService);
    }

    @Test
    void testInstanciacaoResource() {
        // Testa que a classe pode ser instanciada corretamente
        RiskProfileResource resource = new RiskProfileResource();
        assertNotNull(resource);
    }

    @Test
    void perfilRisco_deveFuncionarComPerfilComCamposNulos() {
        // Arrange
        Long clienteId = 1L;
        RiskProfileResponseDTO perfilEsperado = new RiskProfileResponseDTO();
        perfilEsperado.perfil = null;
        perfilEsperado.pontuacao = 0;

        when(riskProfileService.calculateProfile(clienteId)).thenReturn(perfilEsperado);

        // Act
        RiskProfileResponseDTO result = riskProfileResource.perfilRisco(clienteId);

        // Assert
        assertNotNull(result);
        assertNull(result.perfil);
        assertEquals(0, result.pontuacao);
        verify(riskProfileService).calculateProfile(clienteId);
        verify(telemetryService).record(eq("perfil-risco"), anyLong());
    }

    @Test
    void perfilRisco_deveFuncionarComPerfilComScoreMaximo() {
        // Arrange
        Long clienteId = 1L;
        RiskProfileResponseDTO perfilEsperado = new RiskProfileResponseDTO();
        perfilEsperado.perfil = "ARROJADO";
        perfilEsperado.pontuacao = 100;

        when(riskProfileService.calculateProfile(clienteId)).thenReturn(perfilEsperado);

        // Act
        RiskProfileResponseDTO result = riskProfileResource.perfilRisco(clienteId);

        // Assert
        assertNotNull(result);
        assertEquals("ARROJADO", result.perfil);
        assertEquals(100, result.pontuacao);
        verify(riskProfileService).calculateProfile(clienteId);
        verify(telemetryService).record(eq("perfil-risco"), anyLong());
    }
}