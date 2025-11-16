package cef.financial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Produto de investimento parametrizado, consultado pela API
 * para validar entrada e montar simulações.
 *
 * Mapeado para a tabela HTE_INVESTMENT_PRODUCT.
 */
@Entity
@Table(name = "HTE_INVESTMENT_PRODUCT")
public class InvestmentProduct extends PanacheEntityBase {

    @Id
    @Column(name = "rn_")
    public Integer rn; // usamos o rn_ como identificador lógico para o JPA

    @Column(name = "id")
    public Integer externalId; // campo id da tabela, pode ser usado como id de negócio

    @Column(name = "liquidez_dias")
    public Integer liquidezDias;

    @Column(name = "prazo_max_meses")
    public Integer prazoMaxMeses;

    @Column(name = "prazo_min_meses")
    public Integer prazoMinMeses;

    @Column(name = "rentabilidade_anual")
    public Double rentabilidadeAnual; // ex.: 0.12 = 12% ao ano

    @Column(name = "hib_sess_id", length = 36)
    public String hibSessId;

    @Column(name = "nome", length = 255)
    public String nome;

    @Column(name = "perfil_recomendado", length = 255)
    public String perfilRecomendado; // Conservador / Moderado / Agressivo

    @Column(name = "risco", length = 255)
    public String risco; // Baixo / Médio / Alto, etc.

    @Column(name = "tipo", length = 255)
    public String tipo; // CDB, Tesouro, Fundo, etc.
}
