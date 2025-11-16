package cef.financial.domain.service;

import cef.financial.domain.dto.InvestmentSimulationRequestDTO;
import cef.financial.domain.dto.InvestmentSimulationResponseDTO;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.model.InvestmentSimulation;
import cef.financial.domain.repository.InvestmentProductRepository;
import cef.financial.domain.repository.InvestmentSimulationRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
public class InvestmentSimulationService {

    @Inject
    InvestmentProductRepository productRepository;

    @Inject
    InvestmentSimulationRepository simulationRepository;

    public InvestmentSimulationResponseDTO simulate(InvestmentSimulationRequestDTO request) {

        // findById espera Long → request.produtoId JÁ é Long
        InvestmentProduct product = productRepository.findById(request.produtoId);

        if (product == null) {
            throw new WebApplicationException(
                    "Produto não encontrado: " + request.produtoId,
                    Response.Status.NOT_FOUND
            );
        }

        double taxaAnual = product.rentabilidadeAnual != null ? product.rentabilidadeAnual : 0.0;
        double taxaMensal = Math.pow(1 + taxaAnual, 1.0 / 12.0) - 1.0;

        double valorFinal = request.valor *
                Math.pow(1 + taxaMensal, request.prazoMeses);

        double rentabilidadeEfetiva = request.valor > 0
                ? (valorFinal / request.valor) - 1
                : 0.0;

        OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.UTC);

        InvestmentSimulation sim = new InvestmentSimulation();
        sim.clienteId = request.clienteId;
        sim.produto = product;
        sim.valorInvestido = request.valor;
        sim.valorFinal = valorFinal;
        sim.prazoMeses = request.prazoMeses;
        sim.dataSimulacao = agora;
        sim.persist();

        return new InvestmentSimulationResponseDTO(
                new InvestmentSimulationResponseDTO.ProdutoValidado(
                        product.id,           // Long → bate com DTO
                        product.nome,
                        product.tipo,
                        taxaAnual,
                        product.risco
                ),
                new InvestmentSimulationResponseDTO.ResultadoSimulacao(
                        valorFinal,
                        rentabilidadeEfetiva,
                        request.prazoMeses
                ),
                agora
        );
    }
}
