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

    @Column(name = "NOME", nullable = false, length = 255)
    public String nome;

    @Column(name = "DOCUMENTO", length = 20)
    public String documento; // CPF/CNPJ (opcional)

    @Column(name = "EMAIL", length = 255)
    public String email;

    @Column(name = "PERFIL", length = 50)
    public String perfil; // Conservador / Moderado / Agressivo (se quiser persistir)

    @Column(name = "CRIADO_EM")
    public OffsetDateTime criadoEm;
}
