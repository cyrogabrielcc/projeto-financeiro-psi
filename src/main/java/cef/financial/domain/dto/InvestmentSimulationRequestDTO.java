package cef.financial.domain.dto;



import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvestmentSimulationRequestDTO {


    public Long clienteId;

    @NotNull
    public Long produtoId;

    @Min(1)
    public double valor;

    @Min(1)
    public int prazoMeses;

    @NotNull
    public String tipoProduto; // "CDB", etc.

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public Long getClienteId() {
        return clienteId;
    }


    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }


    public void setTipoProduto(String tipoProduto) {
        this.tipoProduto = tipoProduto;
    }

    public int getPrazoMeses() {
        return prazoMeses;
    }

    public void setPrazoMeses(int prazoMeses) {
        this.prazoMeses = prazoMeses;
    }




}
