package cef.financial.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class CreateInvestmentProductRequestDTO {

    @NotBlank
    public String nome;

    @NotBlank
    public String tipo; // ex: RENDA FIXA, MULTIMERCADO, ACAO...

    @NotNull
    @PositiveOrZero
    public Double rentabilidadeAnual; // em decimal, ex: 0.12 = 12% a.a.

    @PositiveOrZero
    public Integer liquidezDias; // dias para resgate

    @PositiveOrZero
    public Integer prazoMinMeses;

    @PositiveOrZero
    public Integer prazoMaxMeses;

    public String perfilRecomendado; // Conservador / Moderado / Agressivo
    public String risco;             // Baixo / Médio / Alto
}
