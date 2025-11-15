package cef.financial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "investment_product")
public class InvestmentProduct extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // usa AUTOINCREMENT do SQLite
    public Long id;

    @Column(nullable = false)
    public String nome;

    @Column(nullable = false)
    public String tipo;              // CDB, Fundo, LCA etc.

    @Column(name = "rentabilidade_anual")
    public Double rentabilidadeAnual;

    @Column
    public String risco;             // Baixo, Médio, Alto

    @Column(name = "prazo_min_meses")
    public Integer prazoMinimoMeses;

    @Column(name = "prazo_max_meses")
    public Integer prazoMaximoMeses;

    @Column(name = "liquidez_dias")
    public Integer liquidezDias;

    @Column(name = "perfil_recomendado")
    public String perfilRecomendado;
}
