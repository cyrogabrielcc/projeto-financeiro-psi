package cef.financial.domain.service;

import cef.financial.domain.dto.TelemetryResponseDTO;
import cef.financial.domain.model.TelemetryEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class TelemetryService {

    private static final Logger LOG = Logger.getLogger(TelemetryService.class);

    @Transactional
    public void record(String serviceName, long durationMs) {
        try {
            validarParametrosRecord(serviceName, durationMs);

            TelemetryEvent event = new TelemetryEvent();
            event.serviceName = serviceName;
            event.durationMs = durationMs;
            event.timestamp = OffsetDateTime.now();

            event.persist(); // ou persistAndFlush() se você preferir garantir flush imediato
        } catch (WebApplicationException e) {
            // Já é uma exceção controlada, apenas registra e propaga
            LOG.warnf("Erro de validação ao registrar telemetria: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Erro inesperado ao registrar telemetria", e);
            throw new WebApplicationException(
                    "Erro interno ao registrar telemetria.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Transactional
    public TelemetryResponseDTO getTelemetry(LocalDate from, LocalDate to) {
        try {
            PeriodoConsulta periodo = validarETratarPeriodo(from, to);

            List<TelemetryEvent> events = TelemetryEvent.list(
                    "timestamp >= ?1 and timestamp < ?2",
                    periodo.start, periodo.endExclusive
            );

            Map<String, List<TelemetryEvent>> byService =
                    events.stream().collect(Collectors.groupingBy(e -> e.serviceName));

            TelemetryResponseDTO response = new TelemetryResponseDTO();
            response.servicos = byService.entrySet().stream()
                    .map(entry -> {
                        TelemetryResponseDTO.ServiceMetric m = new TelemetryResponseDTO.ServiceMetric();
                        m.nome = entry.getKey();
                        m.quantidadeChamadas = entry.getValue().size();
                        m.mediaTempoRespostaMs = entry.getValue().stream()
                                .mapToLong(ev -> ev.durationMs)
                                .average()
                                .orElse(0.0);
                        return m;
                    })
                    .toList();

            TelemetryResponseDTO.Periodo periodoDto = new TelemetryResponseDTO.Periodo();
            periodoDto.inicio = periodo.from;
            periodoDto.fim = periodo.to;
            response.periodo = periodoDto;

            return response;

        } catch (WebApplicationException e) {
            // Erro de validação ou regra de negócio
            LOG.warnf("Erro de validação ao consultar telemetria: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Erro inesperado ao consultar telemetria", e);
            throw new WebApplicationException(
                    "Erro interno ao consultar telemetria.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    // ================== MÉTODOS DE TRATAMENTO/VALIDAÇÃO ==================

    private void validarParametrosRecord(String serviceName, long durationMs) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new WebApplicationException(
                    "Nome do serviço não pode ser nulo ou vazio.",
                    Response.Status.BAD_REQUEST
            );
        }

        if (durationMs < 0) {
            throw new WebApplicationException(
                    "Duração da chamada não pode ser negativa.",
                    Response.Status.BAD_REQUEST
            );
        }
    }

    /**
     * Trata nulos, garante período válido e monta o intervalo de datas.
     */
    private PeriodoConsulta validarETratarPeriodo(LocalDate from, LocalDate to) {
        LocalDate hoje = LocalDate.now();

        // Default: últimos 30 dias se não vier 'from'
        if (from == null) {
            from = hoje.minusDays(30);
        }
        // Default: hoje se não vier 'to'
        if (to == null) {
            to = hoje;
        }

        // Garante que from <= to
        if (from.isAfter(to)) {
            throw new WebApplicationException(
                    "Data inicial não pode ser maior que a data final.",
                    Response.Status.BAD_REQUEST
            );
        }

        ZoneOffset offset = OffsetDateTime.now().getOffset();
        OffsetDateTime start = from.atStartOfDay().atOffset(offset);
        OffsetDateTime endExclusive = to.plusDays(1).atStartOfDay().atOffset(offset);

        PeriodoConsulta periodo = new PeriodoConsulta();
        periodo.from = from;
        periodo.to = to;
        periodo.start = start;
        periodo.endExclusive = endExclusive;
        return periodo;
    }

    // Classe auxiliar só para organizar o período
    private static class PeriodoConsulta {
        LocalDate from;
        LocalDate to;
        OffsetDateTime start;
        OffsetDateTime endExclusive;
    }
}
