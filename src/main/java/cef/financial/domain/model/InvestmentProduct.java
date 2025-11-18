package cef.financial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "HTE_INVESTMENT_PRODUCT")
public class InvestmentProduct extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    public Long id;

    @Column(nullable = false)
    public String nome;

    @Column(nullable = false)
    public String tipo; // CDB, Tesouro, Fundo, etc.

    @Column(nullable = false)
    public String risco; // Conservador / Moderado / Agressivo

    @Column(name = "rentabilidade_anual", nullable = false)
    public Double rentabilidadeAnual;

    @Column(name = "liquidez_dias", nullable = false)
    public Integer liquidezDias;

    @Column(name = "prazo_min_meses", nullable = false)
    public Integer prazoMinMeses;

    @Column(name = "prazo_max_meses", nullable = false)
    public Integer prazoMaxMeses;

    @Column(name = "PERFIL_RECOMENDADO", length = 50)
    public String perfilRecomendado;
}
