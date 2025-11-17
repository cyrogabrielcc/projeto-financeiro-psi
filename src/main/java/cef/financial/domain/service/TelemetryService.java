package cef.financial.domain.service;

import cef.financial.domain.dto.TelemetryResponseDTO;
import cef.financial.domain.model.TelemetryEvent;
import cef.financial.domain.repository.TelemetryEventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    @Inject
    TelemetryEventRepository telemetryEventRepository;

    // construtor padrão para o CDI
    public TelemetryService() {
    }

    // construtor para testes unitários
    public TelemetryService(TelemetryEventRepository telemetryEventRepository) {
        this.telemetryEventRepository = telemetryEventRepository;
    }

    @Transactional
    public void record(String serviceName, long durationMs) {
        try {
            validarParametrosRecord(serviceName, durationMs);

            TelemetryEvent event = new TelemetryEvent();
            event.serviceName = serviceName;
            event.durationMs = durationMs;
            event.timestamp = OffsetDateTime.now();

            telemetryEventRepository.persist(event);

        } catch (WebApplicationException e) {
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

            List<TelemetryEvent> events = telemetryEventRepository.list(
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

    private PeriodoConsulta validarETratarPeriodo(LocalDate from, LocalDate to) {
        LocalDate hoje = LocalDate.now();

        if (from == null) {
            from = hoje.minusDays(30);
        }
        if (to == null) {
            to = hoje;
        }

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

    private static class PeriodoConsulta {
        LocalDate from;
        LocalDate to;
        OffsetDateTime start;
        OffsetDateTime endExclusive;
    }
}
