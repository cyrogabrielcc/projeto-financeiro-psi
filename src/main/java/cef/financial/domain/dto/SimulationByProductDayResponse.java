package cef.financial.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class SimulationByProductDayResponse {

    public String produto;

    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate data;

    public long quantidadeSimulacoes;
    public double mediaValorFinal;
}
