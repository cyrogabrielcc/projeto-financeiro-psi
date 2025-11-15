package cef.financial.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class TelemetryResponse {

    public List<ServiceMetric> servicos;
    public Periodo periodo;

    public static class ServiceMetric {
        public String nome;
        public long quantidadeChamadas;
        public double mediaTempoRespostaMs;
    }

    public static class Periodo {
        @JsonFormat(pattern = "yyyy-MM-dd")
        public LocalDate inicio;

        @JsonFormat(pattern = "yyyy-MM-dd")
        public LocalDate fim;
    }
}
