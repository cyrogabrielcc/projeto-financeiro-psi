package cef.financial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "telemetry_event")
public class TelemetryEvent extends PanacheEntity {

    @Column(nullable = false)
    public String serviceName;

    @Column(nullable = false)
    public long durationMs;

    @Column(nullable = false)
    public OffsetDateTime timestamp;
}
