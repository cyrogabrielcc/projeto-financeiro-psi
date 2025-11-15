package cef.financial.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public class InvestmentSimulationResponseDTO {

    public ProdutoValidado produtoValidado;
    public ResultadoSimulacao resultadoSimulacao;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    public OffsetDateTime dataSimulacao;

    public InvestmentSimulationResponseDTO() {
    }

    public InvestmentSimulationResponseDTO(ProdutoValidado produtoValidado,
                                           ResultadoSimulacao resultadoSimulacao,
                                           OffsetDateTime dataSimulacao) {
        this.produtoValidado = produtoValidado;
        this.resultadoSimulacao = resultadoSimulacao;
        this.dataSimulacao = dataSimulacao;
    }

    public static class ProdutoValidado {
        public Long id;
        public String nome;
        public String tipo;
        public double rentabilidade;
        public String risco;

        public ProdutoValidado() {
        }

        public ProdutoValidado(Long id, String nome, String tipo,
                               double rentabilidade, String risco) {
            this.id = id;
            this.nome = nome;
            this.tipo = tipo;
            this.rentabilidade = rentabilidade;
            this.risco = risco;
        }
    }

    public static class ResultadoSimulacao {
        public double valorFinal;
        public double rentabilidadeEfetiva;
        public int prazoMeses;

        public ResultadoSimulacao() {
        }

        public ResultadoSimulacao(double valorFinal, double rentabilidadeEfetiva, int prazoMeses) {
            this.valorFinal = valorFinal;
            this.rentabilidadeEfetiva = rentabilidadeEfetiva;
            this.prazoMeses = prazoMeses;
        }
    }
}
