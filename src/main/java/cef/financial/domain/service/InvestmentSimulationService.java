package cef.financial.domain.service;

import cef.financial.domain.dto.InvestmentSimulationRequestDTO;
import cef.financial.domain.dto.InvestmentSimulationResponseDTO;
import cef.financial.domain.model.Customer;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.model.InvestmentSimulation;
import cef.financial.domain.repository.CustomerRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class InvestmentSimulationService {

    private static final Logger LOG = Logger.getLogger(InvestmentSimulationService.class);
    private static final int STATUS_UNPROCESSABLE_ENTITY = 422;

    @Inject
    InvestmentProductRepository productRepository;

    @Inject
    InvestmentSimulationRepository simulationRepository;

    @Inject
    CustomerRepository customerRepository; // <<<<<< INJETADO

    @Transactional
    public InvestmentSimulationResponseDTO simulate(InvestmentSimulationRequestDTO request) {
        try {
            // 1) validação básica
            validarRequest(request);

            // 1.1) garante que o cliente exista (ou cria) e obtem o ID real
            Customer cliente = obterOuCriarCliente(request.clienteId);
            Long clienteIdReal = cliente.id;

            // 2) escolhe/valida produto com base nas regras
            InvestmentProduct product = escolherProdutoElegivel(request);

            // 3) valida rentabilidade do produto
            double taxaAnual = validarRentabilidade(product);
            double taxaMensal = Math.pow(1 + taxaAnual, 1.0 / 12.0) - 1.0;

            // 4) cálculo da simulação
            double valorFinal = request.valor *
                    Math.pow(1 + taxaMensal, request.prazoMeses);

            double rentabilidadeEfetiva = (valorFinal / request.valor) - 1;
            OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.UTC);

            // 5) persistência da simulação
            InvestmentSimulation sim = new InvestmentSimulation();
            sim.clienteId = clienteIdReal;   // <<< usa o ID real do cliente
            sim.produto = product;
            sim.valorInvestido = request.valor;
            sim.valorFinal = valorFinal;
            sim.prazoMeses = request.prazoMeses;
            sim.dataSimulacao = agora;

            simulationRepository.persist(sim);

            // 6) resposta
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
            LOG.warnf(e, "Erro de validação na simulação de investimento: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Erro inesperado ao simular investimento", e);
            throw new WebApplicationException(
                    "Erro interno ao processar a simulação de investimento.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    // ============================================================
    // Cliente: obter ou criar
    // ============================================================
    private Customer obterOuCriarCliente(Long clienteIdRequest) {
        // Primeiro tenta achar um cliente com esse ID
        Customer existente = customerRepository.findById(clienteIdRequest);
        if (existente != null) {
            LOG.debugf("Cliente %d encontrado na base.", clienteIdRequest);
            return existente;
        }

        // Se não encontrou, cria um novo cliente "básico"
        Customer novo = new Customer();
        novo.perfil = "INDEFINIDO";
        novo.rendaMensal = null;
        novo.criadoEm = OffsetDateTime.now(ZoneOffset.UTC);

        customerRepository.persist(novo);
        LOG.infof("Cliente novo criado automaticamente. ID solicitado=%d, ID gerado=%d",
                clienteIdRequest, novo.id);

        return novo;
    }

    // ============================================================
    // Validações de entrada
    // ============================================================
    private void validarRequest(InvestmentSimulationRequestDTO request) {

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

        if (request.produtoId != null && request.produtoId <= 0) {
            throw new WebApplicationException(
                    "produtoId, se informado, deve ser maior que zero.",
                    Response.Status.BAD_REQUEST
            );
        }

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
    }

    // ============================================================
    // Escolha / validação de produto  (igual ao seu)
    // ============================================================
    private InvestmentProduct escolherProdutoElegivel(InvestmentSimulationRequestDTO request) {
        if (request.produtoId != null && request.produtoId > 0) {

            InvestmentProduct product = productRepository.findById(request.produtoId);

            if (product == null) {
                throw new WebApplicationException(
                        "Produto não encontrado: " + request.produtoId,
                        Response.Status.NOT_FOUND
                );
            }

            if (!atendePrazo(product, request.prazoMeses)) {
                throw new WebApplicationException(
                        String.format(
                                "Produto %d não atende ao prazo solicitado (%d meses). " +
                                        "Faixa permitida: [%s, %s] meses.",
                                product.id,
                                request.prazoMeses,
                                product.prazoMinMeses != null ? product.prazoMinMeses : "-",
                                (product.prazoMaxMeses != null && product.prazoMaxMeses > 0)
                                        ? product.prazoMaxMeses : "-"
                        ),
                        Response.Status.BAD_REQUEST
                );
            }

            LOG.infof("Produto escolhido explicitamente pelo cliente: id=%d, nome=%s",
                    product.id, product.nome);
            return product;
        }

        List<InvestmentProduct> todosProdutos = productRepository.listAll();
        if (todosProdutos.isEmpty()) {
            throw new WebApplicationException(
                    "Nenhum produto de investimento cadastrado no sistema.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }

        List<InvestmentProduct> elegiveis = todosProdutos.stream()
                .filter(p -> atendePrazo(p, request.prazoMeses))
                .filter(p -> tipoCompativel(p, request.tipoProduto))
                .collect(Collectors.toList());

        if (elegiveis.isEmpty()) {
            throw new WebApplicationException(
                    "Nenhum produto atende aos parâmetros informados (prazo/tipo).",
                    STATUS_UNPROCESSABLE_ENTITY
            );
        }

        InvestmentProduct escolhido = elegiveis.stream()
                .filter(p -> p.rentabilidadeAnual != null)
                .max(Comparator.comparingDouble(p -> p.rentabilidadeAnual))
                .orElse(elegiveis.get(0));

        LOG.infof("Produto escolhido automaticamente: id=%d, nome=%s, tipo=%s, taxaAnual=%.4f",
                escolhido.id, escolhido.nome, escolhido.tipo,
                escolhido.rentabilidadeAnual != null ? escolhido.rentabilidadeAnual : 0.0);

        return escolhido;
    }

    private boolean atendePrazo(InvestmentProduct product, int prazoMesesRequest) {
        Integer min = product.prazoMinMeses;
        Integer max = product.prazoMaxMeses;

        if (min != null && prazoMesesRequest < min) {
            return false;
        }
        if (max != null && max > 0 && prazoMesesRequest > max) {
            return false;
        }
        return true;
    }

    private boolean tipoCompativel(InvestmentProduct product, String tipoProdutoRequest) {
        if (tipoProdutoRequest == null || tipoProdutoRequest.isBlank()) {
            return true;
        }
        return Objects.equals(
                product.tipo != null ? product.tipo.toUpperCase() : null,
                tipoProdutoRequest.toUpperCase()
        );
    }

    // ============================================================
    // Validação de rentabilidade
    // ============================================================
    private double validarRentabilidade(InvestmentProduct product) {
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

        return taxaAnualObj;
    }
}
