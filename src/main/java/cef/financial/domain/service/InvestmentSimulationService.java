package cef.financial.domain.service;

import cef.financial.domain.dto.InvestmentSimulationRequestDTO;
import cef.financial.domain.dto.InvestmentSimulationResponseDTO;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.model.InvestmentSimulation;
import cef.financial.domain.repository.InvestmentProductRepository;
import cef.financial.domain.repository.InvestmentSimulationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
public class InvestmentSimulationService {

    private static final Logger LOG = Logger.getLogger(InvestmentSimulationService.class);

    @Inject
    InvestmentProductRepository productRepository;

    @Inject
    InvestmentSimulationRepository simulationRepository;

    @Transactional
    public InvestmentSimulationResponseDTO simulate(InvestmentSimulationRequestDTO request) {

        try {
            // ===== 1. Validação básica do request =====
            if (request == null) {
                throw new WebApplicationException(
                        "Requisição de simulação não pode ser nula.",
                        Response.Status.BAD_REQUEST
                );
            }

            if (request.clienteId == null || request.clienteId <= 0) {
                throw new WebApplicationException(
                        "clienteId deve ser informado e maior que zero.",
                        Response.Status.BAD_REQUEST
                );
            }

            if (request.produtoId == null || request.produtoId <= 0) {
                throw new WebApplicationException(
                        "produtoId deve ser informado e maior que zero.",
                        Response.Status.BAD_REQUEST
                );
            }
// -------------
            if (request.valor <= 0) {
                throw new WebApplicationException(
                        "valor deve ser informado e maior que zero.",
                        Response.Status.BAD_REQUEST
                );
            }

            if (request.prazoMeses <= 0) {
                throw new WebApplicationException(
                        "prazoMeses deve ser informado e maior que zero.",
                        Response.Status.BAD_REQUEST
                );
            }
// -------------

            // ===== 2. Busca do produto =====
            InvestmentProduct product = productRepository.findById(request.produtoId);

            if (product == null) {
                throw new WebApplicationException(
                        "Produto não encontrado: " + request.produtoId,
                        Response.Status.NOT_FOUND
                );
            }

            // ===== 3. Validação da rentabilidade =====
            Double taxaAnualObj = product.rentabilidadeAnual;
            if (taxaAnualObj == null) {
                throw new WebApplicationException(
                        "Rentabilidade anual não definida para o produto " + product.id,
                        Response.Status.BAD_REQUEST
                );
            }
            if (taxaAnualObj < 0) {
                throw new WebApplicationException(
                        "Rentabilidade anual inválida (negativa) para o produto " + product.id,
                        Response.Status.BAD_REQUEST
                );
            }

            double taxaAnual = taxaAnualObj;
            double taxaMensal = Math.pow(1 + taxaAnual, 1.0 / 12.0) - 1.0;

            // ===== 4. Cálculo da simulação =====
            double valorFinal = request.valor *
                    Math.pow(1 + taxaMensal, request.prazoMeses);

            double rentabilidadeEfetiva = (valorFinal / request.valor) - 1;

            OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.UTC);

            // ===== 5. Persistência da simulação =====
            InvestmentSimulation sim = new InvestmentSimulation();
            sim.clienteId = request.clienteId;
            sim.produto = product;
            sim.valorInvestido = request.valor;
            sim.valorFinal = valorFinal;
            sim.prazoMeses = request.prazoMeses;
            sim.dataSimulacao = agora;

            // você já tem o repository, então vamos usá-lo:
            simulationRepository.persist(sim);
            // (sim.persist() também funciona, mas assim fica mais consistente)

            // ===== 6. Montagem da resposta =====
            return new InvestmentSimulationResponseDTO(
                    new InvestmentSimulationResponseDTO.ProdutoValidado(
                            product.id,
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

        } catch (WebApplicationException e) {
            // Erro conhecido (regra de negócio / validação): só loga no nível WARN e propaga
            LOG.warnf(e, "Erro de validação na simulação de investimento: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Erro inesperado: log completo e devolve 500 genérico
            LOG.error("Erro inesperado ao simular investimento", e);
            throw new WebApplicationException(
                    "Erro interno ao processar a simulação de investimento.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }
}
