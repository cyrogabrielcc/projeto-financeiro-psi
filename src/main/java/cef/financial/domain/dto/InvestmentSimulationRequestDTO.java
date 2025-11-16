package cef.financial.domain.dto;



import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class InvestmentSimulationRequestDTO {

    @NotNull
    public Integer clienteId;

    @NotNull
    public Long produtoId;

    @Min(1)
    public double valor;

    @Min(1)
    public int prazoMeses;

    @NotNull
    public String tipoProduto; // "CDB", etc.
}
