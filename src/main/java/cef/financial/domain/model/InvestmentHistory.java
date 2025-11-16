package cef.financial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Representa um investimento REALIZADO pelo cliente.
 * Usado pelo RiskProfileService para calcular o perfil de risco.
 */
@Entity
@Table(name = "HTE_INVESTMENT_HISTORY")
public class InvestmentHistory extends PanacheEntity {

    @Column(name = "CLIENTE_ID", nullable = false)
    public Long clienteId;

    @Column(name = "TIPO", length = 100)
    public String tipo; // ex: "CDB", "Fundo Multimercado", etc.

    @Column(name = "VALOR", nullable = false)
    public double valor;

    @Column(name = "RENTABILIDADE", nullable = false)
    public double rentabilidade; // ex: 0.12 = 12%

    @Column(name = "DATA_INVESTIMENTO")
    public LocalDate dataInvestimento;
}