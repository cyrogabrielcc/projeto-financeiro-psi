package cef.financial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "investment_product")
public class InvestmentProduct extends PanacheEntity {

    @Column(nullable = false)
    public String nome;

    @Column(nullable = false)
    public String tipo; // "CDB", "Fundo", etc.

    @Column(name = "rentabilidade_anual", nullable = false)
    public double rentabilidadeAnual; // ex: 0.12 = 12% a.a.

    @Column(nullable = false)
    public String risco; // "Baixo", "Médio", "Alto"

    @Column(name = "prazo_min_meses")
    public Integer prazoMinimoMeses;

    @Column(name = "prazo_max_meses")
    public Integer prazoMaximoMeses;

    @Column(name = "liquidez_dias")
    public Integer liquidezDias; // D+0, D+1 etc.

    @Column(name = "perfil_recomendado")
    public String perfilRecomendado; // "Conservador", "Moderado", "Agressivo"
}
