package cef.financial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "investment_history")
public class InvestmentHistory extends PanacheEntity {

    @Column(nullable = false)
    public Long clienteId;

    @Column(nullable = false)
    public String tipo;

    @Column(nullable = false)
    public double valor;

    @Column(nullable = false)
    public double rentabilidade;

    @Column(nullable = false)
    public LocalDate data;
}
