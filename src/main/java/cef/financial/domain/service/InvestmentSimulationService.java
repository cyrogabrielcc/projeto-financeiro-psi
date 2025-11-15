package cef.financial.domain.service;

import cef.financial.domain.dto.InvestmentSimulationRequest;
import cef.financial.domain.dto.InvestmentSimulationResponse;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.model.InvestmentSimulation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
public class InvestmentSimulationService {

    private final TelemetryService telemetryService;

    public InvestmentSimulationService(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @Transactional
    public InvestmentSimulationResponse simulate(InvestmentSimulationRequest request) {
        long start = System.currentTimeMillis();
        try {
            List<InvestmentProduct> candidates = InvestmentProduct.list(
                    "tipo = ?1 and (prazoMinimoMeses is null or prazoMinimoMeses <= ?2) " +
                            "and (prazoMaximoMeses is null or prazoMaximoMeses >= ?2)",
                    request.tipoProduto,
                    request.prazoMeses
            );

            if (candidates.isEmpty()) {
                throw new IllegalArgumentException("Nenhum produto compatível com os parâmetros informados.");
            }

            // 2. Para simplificar, usa o primeiro produto que bater
            InvestmentProduct product = candidates.get(0);

            // 3. Cálculo de simulação (juros compostos simples: anual -> meses)
            double taxaMensal = Math.pow(1.0 + product.rentabilidadeAnual, 1.0 / 12.0) - 1.0;
            double valorFinal = request.valor * Math.pow(1.0 + taxaMensal, request.prazoMeses);
            double rentabilidadeEfetiva = (valorFinal / request.valor) - 1.0;

            OffsetDateTime agora = OffsetDateTime.now();

            // 4. Persistir simulação
            InvestmentSimulation sim = new InvestmentSimulation();
            sim.clienteId = request.clienteId;
            sim.produto = product;
            sim.valorInvestido = request.valor;
            sim.valorFinal = valorFinal;
            sim.prazoMeses = request.prazoMeses;
            sim.dataSimulacao = agora;
            sim.persist();

            // 5. Montar envelope de resposta
            InvestmentSimulationResponse.ProdutoValidado produtoValidado =
                    new InvestmentSimulationResponse.ProdutoValidado(
                            product.id,
                            product.nome,
                            product.tipo,
                            product.rentabilidadeAnual,
                            product.risco
                    );

            InvestmentSimulationResponse.ResultadoSimulacao resultado =
                    new InvestmentSimulationResponse.ResultadoSimulacao(
                            round(valorFinal, 2),
                            round(rentabilidadeEfetiva, 4),
                            request.prazoMeses
                    );

            return new InvestmentSimulationResponse(produtoValidado, resultado, agora);
        } finally {
            long duration = System.currentTimeMillis() - start;
            telemetryService.record("simular-investimento", duration);
        }
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }
}
