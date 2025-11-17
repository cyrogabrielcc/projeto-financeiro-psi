package cef.financial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "HTE_INVESTMENT_SIMULATION")
public class InvestmentSimulation extends PanacheEntity {

    @Column(nullable = false)
    public Long clienteId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id")
    public InvestmentProduct produto;

    @Column(nullable = false)
    public double valorInvestido;

    @Column(nullable = false)
    public double valorFinal;

    @Column(nullable = false)
    public int prazoMeses;

    @Column(nullable = false)
    public OffsetDateTime dataSimulacao;
}
