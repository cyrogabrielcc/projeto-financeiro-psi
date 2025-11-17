package cef.financial.domain.repository;

import cef.financial.domain.model.TelemetryEvent;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
public class TelemetryEventRepository implements PanacheRepository<TelemetryEvent> {

    /**
     * Usa exatamente a mesma query que você já usa hoje no TelemetryService
     * quando chamava TelemetryEvent.list(...).
     */
    public List<TelemetryEvent> findByPeriod(OffsetDateTime start, OffsetDateTime end) {
        // ajuste o nome do campo de data conforme a sua entidade (ex.: "timestamp", "dataHora", etc.)
        return list("timestamp between ?1 and ?2 order by timestamp", start, end);
    }
}