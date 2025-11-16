package cef.financial.domain.service;

import cef.financial.domain.dto.InvestmentSimulationRequestDTO;
import cef.financial.domain.dto.InvestmentSimulationResponseDTO;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.model.InvestmentSimulation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
public class InvestmentSimulationService {

    @Transactional
    public InvestmentSimulationResponseDTO simulate(InvestmentSimulationRequestDTO request) {

        List<InvestmentProduct> candidates = InvestmentProduct.list(
                "tipo = ?1 and (prazoMinimoMeses is null or prazoMinimoMeses <= ?2) " +
                        "and (prazoMaximoMeses is null or prazoMaximoMeses >= ?2)",
                request.tipoProduto,
                request.prazoMeses
        );

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Nenhum produto compatível com os parâmetros informados.");
        }

        InvestmentProduct produto = candidates.get(0);

        double valorFinal = request.valor * Math.pow(1 + produto.rentabilidadeAnual / 12.0, request.prazoMeses);
        double rentabilidadeEfetiva = (valorFinal / request.valor) - 1;

        InvestmentSimulation sim = new InvestmentSimulation();
        sim.clienteId = request.clienteId;
        sim.produto = produto;
        sim.valorInvestido = request.valor;
        sim.valorFinal = valorFinal;
        sim.prazoMeses = request.prazoMeses;
        sim.dataSimulacao = OffsetDateTime.now();
        sim.persist();

        InvestmentSimulationResponseDTO.ProdutoValidado produtoDTO =
                new InvestmentSimulationResponseDTO.ProdutoValidado(
                        produto.externalId,
                        produto.nome,
                        produto.tipo,
                        produto.rentabilidadeAnual,
                        produto.risco
                );

        InvestmentSimulationResponseDTO.ResultadoSimulacao resultadoDTO =
                new InvestmentSimulationResponseDTO.ResultadoSimulacao(
                        valorFinal,
                        rentabilidadeEfetiva,
                        request.prazoMeses
                );

        return new InvestmentSimulationResponseDTO(
                produtoDTO,
                resultadoDTO,
                sim.dataSimulacao
        );
    }
}
