package cef.financial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Cliente da base, usado para simulações, histórico e perfil de risco.
 *
 * Mapeado para HTE_CUSTOMER.
 */
@Entity
@Table(name = "HTE_CUSTOMER")
public class Customer extends PanacheEntity {

    @Column(name = "PERFIL", length = 50)
    public String perfil; // Conservador / Moderado / Agressivo (se quiser persistir)

    @Column(name = "CRIADO_EM")
    public OffsetDateTime criadoEm;

    @Column(name = "RENDA_MENSAL")
    public Double rendaMensal;
}
