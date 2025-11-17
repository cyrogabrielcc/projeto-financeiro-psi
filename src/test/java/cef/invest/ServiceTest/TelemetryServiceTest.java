package cef.invest.ServiceTest;

import cef.financial.domain.dto.TelemetryResponseDTO;
import cef.financial.domain.model.TelemetryEvent;
import cef.financial.domain.repository.TelemetryEventRepository;
import cef.financial.domain.service.TelemetryService;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {

    @Mock
    TelemetryEventRepository telemetryEventRepository;

    TelemetryService telemetryService;

    @BeforeEach
    void setUp() {
        telemetryService = new TelemetryService(telemetryEventRepository);
    }

    @Test
    void record_Sucesso() {
        assertDoesNotThrow(() ->
                telemetryService.record("servico-x", 150L)
        );

        verify(telemetryEventRepository, times(1))
                .persist(any(TelemetryEvent.class));
    }

    @Test
    void record_Falha_ParametroInvalido_NomeVazio() {
        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> telemetryService.record("  ", 150L)
        );
        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void record_Falha_ErroInterno() {
        doThrow(new RuntimeException("falha de banco"))
                .when(telemetryEventRepository)
                .persist(any(TelemetryEvent.class));

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> telemetryService.record("servico-x", 150L)
        );

        assertEquals("Erro interno ao registrar telemetria.", ex.getMessage());
        assertEquals(500, ex.getResponse().getStatus());
    }

    @Test
    void getTelemetry_Sucesso_PeriodoPadrao() {
        TelemetryEvent ev1 = new TelemetryEvent();
        ev1.serviceName = "servico-x";
        ev1.durationMs = 100;
        ev1.timestamp = OffsetDateTime.now().minusDays(1);

        TelemetryEvent ev2 = new TelemetryEvent();
        ev2.serviceName = "servico-x";
        ev2.durationMs = 200;
        ev2.timestamp = OffsetDateTime.now().minusDays(2);

        when(telemetryEventRepository.list(anyString(), ArgumentMatchers.<Object>any(), any()))
                .thenReturn(List.of(ev1, ev2));

        TelemetryResponseDTO dto = telemetryService.getTelemetry(null, null);

        assertNotNull(dto);
        assertNotNull(dto.servicos);
        assertFalse(dto.servicos.isEmpty());
        assertEquals("servico-x", dto.servicos.get(0).nome);
        assertEquals(2, dto.servicos.get(0).quantidadeChamadas);
        assertEquals(150.0, dto.servicos.get(0).mediaTempoRespostaMs);
    }

    @Test
    void getTelemetry_Sucesso_AgregacaoPorServico() {
        TelemetryEvent ev1 = new TelemetryEvent();
        ev1.serviceName = "servico-a";
        ev1.durationMs = 100;
        ev1.timestamp = OffsetDateTime.now().minusDays(1);

        TelemetryEvent ev2 = new TelemetryEvent();
        ev2.serviceName = "servico-b";
        ev2.durationMs = 300;
        ev2.timestamp = OffsetDateTime.now().minusDays(1);

        TelemetryEvent ev3 = new TelemetryEvent();
        ev3.serviceName = "servico-a";
        ev3.durationMs = 200;
        ev3.timestamp = OffsetDateTime.now().minusDays(2);

        when(telemetryEventRepository.list(anyString(), Optional.ofNullable(any()), any()))
                .thenReturn(List.of(ev1, ev2, ev3));

        TelemetryResponseDTO dto = telemetryService.getTelemetry(
                LocalDate.now().minusDays(5),
                LocalDate.now()
        );

        assertEquals(2, dto.servicos.size());
        // aqui você pode fazer asserts mais específicos se quiser
    }

    @Test
    void getTelemetry_Falha_DataInvalida() {
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().minusDays(1);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> telemetryService.getTelemetry(from, to)
        );

        assertEquals(400, ex.getResponse().getStatus());
        assertEquals("Data inicial não pode ser maior que a data final.", ex.getMessage());
    }
}
