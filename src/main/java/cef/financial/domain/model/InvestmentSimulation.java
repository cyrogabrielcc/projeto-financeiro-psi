package cef.financial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;


@Entity
@Table(name = "investment_simulation")
public class InvestmentSimulation extends PanacheEntity {

    @Column(nullable = false)
    public Long clienteId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id") // coluna de FK na tabela investment_simulation
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
