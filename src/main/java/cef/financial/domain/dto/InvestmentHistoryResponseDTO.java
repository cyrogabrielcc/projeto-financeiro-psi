package cef.financial.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class InvestmentHistoryResponseDTO {

    public Long id;
    public String tipo;
    public double valor;
    public double rentabilidade;

    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate data;
}
