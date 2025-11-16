package cef.financial.domain.service;

import cef.financial.domain.dto.TelemetryResponseDTO;
import cef.financial.domain.model.TelemetryEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class TelemetryService {

    @Transactional
    public void record(String serviceName, long durationMs) {
        TelemetryEvent event = new TelemetryEvent();
        event.serviceName = serviceName;
        event.durationMs = durationMs;
        event.timestamp = OffsetDateTime.now();
        event.persist();
    }

    @Transactional
    public TelemetryResponseDTO getTelemetry(LocalDate from, LocalDate to) {
        if (from == null) {
            from = LocalDate.now().minusDays(30);
        }
        if (to == null) {
            to = LocalDate.now();
        }

        // Converte LocalDate para OffsetDateTime (intervalo [from, to+1) )
        ZoneOffset offset = OffsetDateTime.now().getOffset();
        OffsetDateTime start = from.atStartOfDay().atOffset(offset);
        OffsetDateTime endExclusive = to.plusDays(1).atStartOfDay().atOffset(offset);

        // Busca SOMENTE eventos dentro do período
        List<TelemetryEvent> events = TelemetryEvent.list(
                "timestamp >= ?1 and timestamp < ?2",
                start, endExclusive
        );

        // Agrupa por serviceName
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

        TelemetryResponseDTO.Periodo periodo = new TelemetryResponseDTO.Periodo();
        periodo.inicio = from;
        periodo.fim = to;
        response.periodo = periodo;

        return response;
    }
}
