package cef.financial.domain.service;

import cef.financial.domain.dto.InvestmentSimulationRequestDTO;
import cef.financial.domain.dto.InvestmentSimulationResponseDTO;
import cef.financial.domain.dto.RiskProfileResponseDTO;
import cef.financial.domain.model.Customer;
import cef.financial.domain.model.InvestmentHistory;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.model.InvestmentSimulation;
import cef.financial.domain.repository.CustomerRepository;
import cef.financial.domain.repository.InvestmentHistoryRepository;
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
    CustomerRepository customerRepository;

    @Inject
    InvestmentHistoryRepository historyRepository;

    @Inject
    RiskProfileService riskProfileService;

    @Transactional
    public InvestmentSimulationResponseDTO simulate(InvestmentSimulationRequestDTO request) {
        try {
            // 1) validação básica
            validarRequest(request);

            // 1.1) garante que o cliente exista (ou cria) e obtém o ID real
            Customer cliente = obterOuCriarCliente(request.clienteId);
            Long clienteIdReal = cliente.id;

            // 2) escolhe/valida produto com base nas regras + mini motor de recomendação
            InvestmentProduct product = escolherProdutoElegivel(request, cliente);

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
            sim.clienteId = clienteIdReal;
            sim.produto = product;
            sim.valorInvestido = request.valor;
            sim.valorFinal = valorFinal;
            sim.prazoMeses = request.prazoMeses;
            sim.dataSimulacao = agora;

            simulationRepository.persist(sim);
            LOG.infof("Simulação persistida para cliente=%d, produto=%d, valor=%.2f, prazo=%d",
                    clienteIdReal, product.id, request.valor, request.prazoMeses);

            // 6) registrar operação no histórico do cliente
            InvestmentHistory hist = new InvestmentHistory();
            hist.clienteId = clienteIdReal;
            hist.tipo = product.tipo != null ? product.tipo : product.nome;
            hist.valor = request.valor;
            hist.rentabilidade = rentabilidadeEfetiva; // retorno da simulação
            hist.dataInvestimento = agora.toLocalDate();

            historyRepository.persist(hist);
            LOG.infof("Histórico de investimento registrado para cliente=%d, tipo=%s, rentabilidade=%.4f",
                    clienteIdReal, hist.tipo, hist.rentabilidade);

            // 7) recalcular o perfil de risco do cliente com base no histórico atualizado
            RiskProfileResponseDTO perfilAtualizado = riskProfileService.calculateProfile(clienteIdReal);
            LOG.infof("Perfil de risco recalculado para cliente=%d: perfil=%s, score=%d",
                    clienteIdReal, perfilAtualizado.perfil, perfilAtualizado.pontuacao);

            // 8) resposta
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

    // ================= Cliente =================
    private Customer obterOuCriarCliente(Long clienteIdRequest) {
        Customer existente = customerRepository.findById(clienteIdRequest);
        if (existente != null) {
            LOG.debugf("Cliente %d encontrado na base. Perfil atual=%s",
                    clienteIdRequest, existente.perfil);
            return existente;
        }

        Customer novo = new Customer();
        novo.id = clienteIdRequest;
        novo.perfil = "INDEFINIDO";
        novo.criadoEm = OffsetDateTime.now(ZoneOffset.UTC);

        customerRepository.persist(novo);
        LOG.infof("Cliente novo criado automaticamente com ID da requisição. ID=%d", novo.id);

        return novo;
    }

    // ================= Validações =================
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

    // ================= Produto + Motor de Recomendação =================
    private InvestmentProduct escolherProdutoElegivel(InvestmentSimulationRequestDTO request,
                                                      Customer cliente) {

        // 1) Se o cliente escolheu produto explicitamente, só valida prazo + compatibilidade com perfil
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

            if (!produtoCompatívelComPerfil(cliente, product)) {
                LOG.warnf(
                        "Produto explicitamente escolhido (%d) não é compatível com o perfil do cliente (%s).",
                        product.id,
                        cliente != null ? cliente.perfil : "N/D"
                );
                // aqui podemos optar por lançar erro se quiser ser rígido
            }

            LOG.infof("Produto escolhido explicitamente pelo cliente: id=%d, nome=%s",
                    product.id, product.nome);
            return product;
        }

        // 2) Nenhum produto escolhido → usar motor de recomendação
        List<InvestmentProduct> todosProdutos = productRepository.listAll();
        if (todosProdutos.isEmpty()) {
            throw new WebApplicationException(
                    "Nenhum produto de investimento cadastrado no sistema.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }

        // 2.1) Filtro básico: prazo e tipo
        List<InvestmentProduct> elegiveisBase = todosProdutos.stream()
                .filter(p -> atendePrazo(p, request.prazoMeses))
                .filter(p -> tipoCompativel(p, request.tipoProduto))
                .collect(Collectors.toList());

        if (elegiveisBase.isEmpty()) {
            throw new WebApplicationException(
                    "Nenhum produto atende aos parâmetros informados (prazo/tipo).",
                    STATUS_UNPROCESSABLE_ENTITY
            );
        }

        // 2.2) Filtro por perfil do cliente
        List<InvestmentProduct> elegiveisPerfil = filtrarPorPerfilCliente(cliente, elegiveisBase);

        List<InvestmentProduct> baseParaRanking =
                elegiveisPerfil.isEmpty() ? elegiveisBase : elegiveisPerfil;

        if (elegiveisPerfil.isEmpty()) {
            LOG.info("Motor de recomendação: nenhum produto compatível com o perfil do cliente. " +
                    "Usando apenas filtros de prazo/tipo.");
        } else {
            LOG.infof("Motor de recomendação: %d produtos compatíveis com o perfil do cliente.",
                    elegiveisPerfil.size());
        }

        // 2.3) Dentro dos produtos ainda elegíveis, decidir entre liquidez vs rentabilidade
        InvestmentProduct escolhido =
                escolherPorPreferenciaLiquidezOuRentabilidade(request, baseParaRanking);

        LOG.infof("Produto escolhido automaticamente pelo motor de recomendação: " +
                        "id=%d, nome=%s, tipo=%s, risco=%s, taxaAnual=%.4f",
                escolhido.id,
                escolhido.nome,
                escolhido.tipo,
                escolhido.risco,
                escolhido.rentabilidadeAnual != null ? escolhido.rentabilidadeAnual : 0.0
        );

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

    // ================= Rentabilidade =================
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

    public List<InvestmentSimulation> listAllSimulations() {
        return simulationRepository.listAll();
    }

    // ===================== Mini Motor de Recomendação =====================

    private boolean produtoCompatívelComPerfil(Customer cliente, InvestmentProduct produto) {
        if (cliente == null || cliente.perfil == null || produto == null) {
            return true; // se não souber o perfil, não bloqueia
        }
        int riscoProduto = riscoScore(produto.risco);
        int maxRiscoCliente = perfilMaxRiskScore(cliente.perfil);
        return riscoProduto <= maxRiscoCliente;
    }

    private List<InvestmentProduct> filtrarPorPerfilCliente(Customer cliente,
                                                            List<InvestmentProduct> produtos) {
        if (cliente == null || cliente.perfil == null || produtos == null || produtos.isEmpty()) {
            return List.of();
        }

        int maxRiscoCliente = perfilMaxRiskScore(cliente.perfil);

        return produtos.stream()
                .filter(p -> riscoScore(p.risco) <= maxRiscoCliente)
                .collect(Collectors.toList());
    }

    private int perfilMaxRiskScore(String perfilCliente) {
        if (perfilCliente == null) return 2; // default = moderado

        String p = perfilCliente.trim().toUpperCase();
        return switch (p) {
            case "CONSERVADOR" -> 1;
            case "MODERADO" -> 2;
            case "AGRESSIVO", "ARROJADO" -> 3;
            default -> 2;
        };
    }

    private int riscoScore(String riscoProduto) {
        if (riscoProduto == null) return 2;

        String r = riscoProduto.trim().toUpperCase();
        return switch (r) {
            case "BAIXO", "BAIXO RISCO" -> 1;
            case "MEDIO", "MÉDIO", "MODERADO" -> 2;
            case "ALTO", "ALTO RISCO" -> 3;
            default -> 2;
        };
    }

    /**
     * Dentro dos produtos compatíveis com prazo/tipo/perfil, decide:
     *
     * - Prazo curto (<= 12 meses)  → prioriza LIQUIDEZ (menor liquidezDias), desempate por maior rentabilidade
     * - Prazo longo  (> 12 meses)  → prioriza RENTABILIDADE (maior rentabilidadeAnual), desempate por menor liquidez
     */
    private InvestmentProduct escolherPorPreferenciaLiquidezOuRentabilidade(
            InvestmentSimulationRequestDTO request,
            List<InvestmentProduct> candidatos) {

        if (candidatos == null || candidatos.isEmpty()) {
            throw new WebApplicationException(
                    "Nenhum produto disponível para recomendação.",
                    STATUS_UNPROCESSABLE_ENTITY
            );
        }

        if (candidatos.size() == 1) {
            return candidatos.get(0);
        }

        boolean prefereLiquidez = request.prazoMeses <= 12;

        if (prefereLiquidez) {
            // prioriza produtos com menor liquidezDias (resgate mais rápido)
            return candidatos.stream()
                    .sorted(
                            Comparator
                                    .comparing((InvestmentProduct p) ->
                                            p.liquidezDias != null ? p.liquidezDias : Integer.MAX_VALUE)
                                    .thenComparing(
                                            (InvestmentProduct p) ->
                                                    p.rentabilidadeAnual != null ? p.rentabilidadeAnual : 0.0,
                                            Comparator.reverseOrder()
                                    )
                    )
                    .findFirst()
                    .orElse(candidatos.get(0));
        } else {
            // prioriza produtos com maior rentabilidadeAnual, desempata pela liquidez
            return candidatos.stream()
                    .sorted(
                            Comparator
                                    .comparing(
                                            (InvestmentProduct p) ->
                                                    p.rentabilidadeAnual != null ? p.rentabilidadeAnual : 0.0,
                                            Comparator.reverseOrder()
                                    )
                                    .thenComparing(
                                            (InvestmentProduct p) ->
                                                    p.liquidezDias != null ? p.liquidezDias : Integer.MAX_VALUE
                                    )
                    )
                    .findFirst()
                    .orElse(candidatos.get(0));
        }
    }
}
