package cef.financial.domain.service;

import cef.financial.domain.dto.TelemetryResponse;
import cef.financial.domain.model.TelemetryEvent;
import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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

    public TelemetryResponse getTelemetry(LocalDate from, LocalDate to) {
        if (from == null) {
            from = LocalDate.now().minusDays(30);
        }
        if (to == null) {
            to = LocalDate.now();
        }

        LocalDate finalFrom = from;
        LocalDate finalTo = to;

        List<TelemetryEvent> events = TelemetryEvent.<TelemetryEvent>findAll().stream()
                .map(e -> (TelemetryEvent) e)
                .filter(e -> {
                    LocalDate d = e.timestamp.toLocalDate();
                    return !d.isBefore(finalFrom) && !d.isAfter(finalTo);
                })
                .collect(Collectors.toList());

        Map<String, List<TelemetryEvent>> byService =
                events.stream().collect(Collectors.groupingBy(e -> e.serviceName));

        TelemetryResponse response = new TelemetryResponse();
        response.servicos = byService.entrySet().stream().map(entry -> {
            TelemetryResponse.ServiceMetric m = new TelemetryResponse.ServiceMetric();
            m.nome = entry.getKey();
            m.quantidadeChamadas = entry.getValue().size();
            m.mediaTempoRespostaMs = entry.getValue().stream()
                    .mapToLong(ev -> ev.durationMs)
                    .average()
                    .orElse(0.0);
            return m;
        }).toList();

        TelemetryResponse.Periodo periodo = new TelemetryResponse.Periodo();
        periodo.inicio = from;
        periodo.fim = to;
        response.periodo = periodo;

        return response;
    }
}
