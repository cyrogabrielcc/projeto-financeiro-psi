package cef.financial.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.time.OffsetDateTime;

public class SimulationHistoryResponseDTO {

    public Long   id;
    public Integer clienteId;
    public String produto;
    public double valorInvestido;
    public double valorFinal;
    public int prazoMeses;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    public OffsetDateTime dataSimulacao;
}
