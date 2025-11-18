package cef.invest.ServiceTest;

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
import cef.financial.domain.service.InvestmentSimulationService;
import cef.financial.domain.service.RiskProfileService;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentSimulationServiceTest {

    @Mock
    InvestmentProductRepository productRepository;

    @Mock
    InvestmentSimulationRepository simulationRepository;

    @Mock
    CustomerRepository customerRepository;

    @Mock
    InvestmentHistoryRepository historyRepository;

    @Mock
    RiskProfileService riskProfileService;

    @InjectMocks
    InvestmentSimulationService simulationService;

    // Helper para criar request padrão "válido"
    private InvestmentSimulationRequestDTO defaultRequest() {
        InvestmentSimulationRequestDTO r = new InvestmentSimulationRequestDTO();
        r.clienteId = 1L;
        r.produtoId = 10L;
        r.valor = 1000.0;
        r.prazoMeses = 12;
        r.tipoProduto = "RENDA_FIXA";
        return r;
    }

    private Customer cliente(String perfil, Long id) {
        Customer c = new Customer();
        c.id = id;
        c.perfil = perfil;
        return c;
    }

    private InvestmentProduct produtoPadrao() {
        InvestmentProduct p = new InvestmentProduct();
        p.id = 10L;
        p.nome = "Produto Teste";
        p.tipo = "RENDA_FIXA";
        p.risco = "MEDIO";
        p.rentabilidadeAnual = 0.10;
        p.prazoMinMeses = 6;
        p.prazoMaxMeses = 24;
        p.liquidezDias = 30;
        return p;
    }

    private RiskProfileResponseDTO perfilMock(String perfil, int score) {
        RiskProfileResponseDTO r = new RiskProfileResponseDTO();
        r.perfil = perfil;
        r.pontuacao = score;
        return r;
    }

    // ==========================================================
    // 1. Caminho feliz: cliente existe + produtoId informado
    // ==========================================================

    @Test
    @DisplayName("1. Deve simular com sucesso quando cliente existe e produtoId é fornecido")
    void simulate_Success_ClienteExiste_ProdutoIdFornecido() {
        InvestmentSimulationRequestDTO request = defaultRequest();

        Customer cliente = cliente("ARROJADO", 1L);
        InvestmentProduct product = produtoPadrao();

        when(customerRepository.findById(1L)).thenReturn(cliente);
        when(productRepository.findById(10L)).thenReturn(product);
        when(riskProfileService.calculateProfile(1L))
                .thenReturn(perfilMock("ARROJADO", 90));

        // não precisamos stubbar persist (método void), só garantir que não explode
        doNothing().when(simulationRepository).persist(any(InvestmentSimulation.class));
        doNothing().when(historyRepository).persist(any(InvestmentHistory.class));

        InvestmentSimulationResponseDTO resp = simulationService.simulate(request);

        assertNotNull(resp);
        assertNotNull(resp.dataSimulacao);
        assertNotNull(resp.produtoValidado);
        assertNotNull(resp.resultadoSimulacao);

        assertEquals(10L, resp.produtoValidado.id);
        assertEquals("Produto Teste", resp.produtoValidado.nome);
        assertEquals("RENDA_FIXA", resp.produtoValidado.tipo);
        assertEquals(0.10, resp.produtoValidado.rentabilidade, 0.0001);

        assertEquals(12, resp.resultadoSimulacao.prazoMeses);
        assertTrue(resp.resultadoSimulacao.valorFinal > request.valor);
        assertTrue(resp.resultadoSimulacao.rentabilidadeEfetiva > 0);

        verify(customerRepository).findById(1L);
        verify(productRepository).findById(10L);
        verify(simulationRepository).persist(any(InvestmentSimulation.class));
        verify(historyRepository).persist(any(InvestmentHistory.class));
        verify(riskProfileService).calculateProfile(1L);
    }

    // ==========================================================
    // 2. Criação automática de cliente novo
    // ==========================================================

    @Test
    @DisplayName("2. Deve criar novo cliente se clienteId não for encontrado")
    void simulate_Success_CriaNovoCliente() {
        InvestmentSimulationRequestDTO request = defaultRequest();
        request.clienteId = 999L;

        when(customerRepository.findById(999L)).thenReturn(null);

        InvestmentProduct product = produtoPadrao();
        when(productRepository.findById(10L)).thenReturn(product);

        doNothing().when(customerRepository).persist(any(Customer.class));
        doNothing().when(simulationRepository).persist(any(InvestmentSimulation.class));
        doNothing().when(historyRepository).persist(any(InvestmentHistory.class));

        when(riskProfileService.calculateProfile(999L))
                .thenReturn(perfilMock("INDEFINIDO", 0));

        InvestmentSimulationResponseDTO resp = simulationService.simulate(request);

        assertNotNull(resp);

        // captura do cliente persistido
        ArgumentCaptor<Customer> clienteCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).persist(clienteCaptor.capture());

        Customer salvo = clienteCaptor.getValue();
        assertEquals(999L, salvo.id);
        assertEquals("INDEFINIDO", salvo.perfil);
    }

    // ==========================================================
    // 3. Auto-seleção de produto (motor de recomendação)
    // ==========================================================

    @Test
    @DisplayName("3. Auto-seleção deve escolher produto com melhor liquidez para prazo curto")
    void simulate_Success_AutoSelecaoProduto_PrazoCurto() {
        InvestmentSimulationRequestDTO request = defaultRequest();
        request.produtoId = null; // força motor de recomendação
        request.prazoMeses = 6;
        request.tipoProduto = "RENDA_FIXA";

        Customer cliente = cliente("MODERADO", 1L);
        when(customerRepository.findById(1L)).thenReturn(cliente);

        InvestmentProduct p1 = new InvestmentProduct();
        p1.id = 1L;
        p1.nome = "Renda Fixa Baixa Liquidez";
        p1.tipo = "RENDA_FIXA";
        p1.risco = "MEDIO";
        p1.prazoMinMeses = 3;
        p1.prazoMaxMeses = 12;
        p1.liquidezDias = 60;
        p1.rentabilidadeAnual = 0.12;

        InvestmentProduct p2 = new InvestmentProduct();
        p2.id = 2L;
        p2.nome = "Renda Fixa Alta Liquidez";
        p2.tipo = "RENDA_FIXA";
        p2.risco = "MEDIO";
        p2.prazoMinMeses = 3;
        p2.prazoMaxMeses = 12;
        p2.liquidezDias = 1;         // liquidez melhor
        p2.rentabilidadeAnual = 0.10;

        when(productRepository.listAll()).thenReturn(List.of(p1, p2));

        doNothing().when(simulationRepository).persist(any(InvestmentSimulation.class));
        doNothing().when(historyRepository).persist(any(InvestmentHistory.class));
        when(riskProfileService.calculateProfile(1L))
                .thenReturn(perfilMock("MODERADO", 70));

        InvestmentSimulationResponseDTO resp = simulationService.simulate(request);

        assertNotNull(resp);
        assertNotNull(resp.produtoValidado);
        // pra prazo curto, prioriza liquidez → deve pegar p2
        assertEquals(2L, resp.produtoValidado.id);
    }

    @Test
    @DisplayName("4. Auto-seleção deve escolher produto com melhor rentabilidade para prazo longo")
    void simulate_Success_AutoSelecaoProduto_PrazoLongo() {
        InvestmentSimulationRequestDTO request = defaultRequest();
        request.produtoId = null;
        request.prazoMeses = 36; // prazo longo
        request.tipoProduto = null; // aceita qualquer tipo

        Customer cliente = cliente("MODERADO", 1L);
        when(customerRepository.findById(1L)).thenReturn(cliente);

        InvestmentProduct p1 = new InvestmentProduct();
        p1.id = 1L;
        p1.nome = "Produto Rentabilidade Baixa";
        p1.tipo = "RENDA_FIXA";
        p1.risco = "MEDIO";
        p1.prazoMinMeses = 12;
        p1.prazoMaxMeses = 60;
        p1.liquidezDias = 30;
        p1.rentabilidadeAnual = 0.08;

        InvestmentProduct p2 = new InvestmentProduct();
        p2.id = 2L;
        p2.nome = "Produto Rentabilidade Alta";
        p2.tipo = "RENDA_VARIAVEL";
        p2.risco = "ALTO";
        p2.prazoMinMeses = 12;
        p2.prazoMaxMeses = 60;
        p2.liquidezDias = 90;
        p2.rentabilidadeAnual = 0.15;

        when(productRepository.listAll()).thenReturn(List.of(p1, p2));

        // perfil moderado: risco máximo 2 → produto de risco 3 (ALTO) é filtrado
        when(riskProfileService.calculateProfile(1L))
                .thenReturn(perfilMock("MODERADO", 70));

        doNothing().when(simulationRepository).persist(any(InvestmentSimulation.class));
        doNothing().when(historyRepository).persist(any(InvestmentHistory.class));

        InvestmentSimulationResponseDTO resp = simulationService.simulate(request);

        assertNotNull(resp);
        // só p1 é compatível com perfil → deve ser escolhido
        assertEquals(1L, resp.produtoValidado.id);
    }

    // ==========================================================
    // 5. Validações básicas do request
    // ==========================================================

    @Test
    @DisplayName("5. Deve lançar BAD_REQUEST se request for nula")
    void simulate_Fail_RequestNula() {
        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> simulationService.simulate(null));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Requisição de simulação não pode ser nula"));
    }

    @Test
    @DisplayName("6. Deve lançar BAD_REQUEST se clienteId for inválido")
    void simulate_Fail_ClienteIdInvalido() {
        InvestmentSimulationRequestDTO request = defaultRequest();
        request.clienteId = 0L;

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> simulationService.simulate(request));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("clienteId deve ser informado"));
    }

    @Test
    @DisplayName("7. Deve lançar BAD_REQUEST se produtoId for inválido (<=0)")
    void simulate_Fail_ProdutoIdInvalido() {
        InvestmentSimulationRequestDTO request = defaultRequest();
        request.produtoId = 0L;

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> simulationService.simulate(request));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("produtoId, se informado, deve ser maior que zero."));
    }

    @Test
    @DisplayName("8. Deve lançar BAD_REQUEST se valor for menor ou igual a zero")
    void simulate_Fail_ValorInvalido() {
        InvestmentSimulationRequestDTO request = defaultRequest();
        request.valor = 0.0;

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> simulationService.simulate(request));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("valor deve ser informado e maior que zero"));
    }

    @Test
    @DisplayName("9. Deve lançar BAD_REQUEST se prazoMeses for menor ou igual a zero")
    void simulate_Fail_PrazoInvalido() {
        InvestmentSimulationRequestDTO request = defaultRequest();
        request.prazoMeses = 0;

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> simulationService.simulate(request));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("prazoMeses deve ser informado e maior que zero"));
    }

    // ==========================================================
    // 6. Erros relacionados ao produto
    // ==========================================================

    @Test
    @DisplayName("10. Deve lançar NOT_FOUND se produtoId não existir")
    void simulate_Fail_ProdutoNaoEncontrado() {
        InvestmentSimulationRequestDTO request = defaultRequest();

        when(customerRepository.findById(1L)).thenReturn(cliente("CONSERVADOR", 1L));
        when(productRepository.findById(10L)).thenReturn(null);

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> simulationService.simulate(request));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Produto não encontrado: 10"));
    }

    @Test
    @DisplayName("11. Deve lançar BAD_REQUEST se produto não atender ao prazo")
    void simulate_Fail_ProdutoNaoAtendePrazo() {
        InvestmentSimulationRequestDTO request = defaultRequest();
        request.prazoMeses = 36; // fora da faixa do produto

        when(customerRepository.findById(1L)).thenReturn(cliente("MODERADO", 1L));
        InvestmentProduct product = produtoPadrao();
        product.prazoMaxMeses = 24;
        when(productRepository.findById(10L)).thenReturn(product);

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> simulationService.simulate(request));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("não atende ao prazo solicitado"));
    }

    @Test
    @DisplayName("12. Deve lançar BAD_REQUEST se rentabilidade do produto for nula")
    void simulate_Fail_RentabilidadeNula() {
        InvestmentSimulationRequestDTO request = defaultRequest();

        when(customerRepository.findById(1L)).thenReturn(cliente("MODERADO", 1L));
        InvestmentProduct product = produtoPadrao();
        product.rentabilidadeAnual = null;
        when(productRepository.findById(10L)).thenReturn(product);

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> simulationService.simulate(request));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Rentabilidade anual não definida"));
    }

    @Test
    @DisplayName("13. Deve lançar BAD_REQUEST se rentabilidade do produto for negativa")
    void simulate_Fail_RentabilidadeNegativa() {
        InvestmentSimulationRequestDTO request = defaultRequest();

        when(customerRepository.findById(1L)).thenReturn(cliente("MODERADO", 1L));
        InvestmentProduct product = produtoPadrao();
        product.rentabilidadeAnual = -0.01;
        when(productRepository.findById(10L)).thenReturn(product);

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> simulationService.simulate(request));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Rentabilidade anual inválida"));
    }

    @Test
    @DisplayName("14. Auto-seleção deve lançar UNPROCESSABLE_ENTITY se nenhum produto for elegível")
    void simulate_Fail_NenhumProdutoElegivel() {
        InvestmentSimulationRequestDTO request = defaultRequest();
        request.produtoId = null;
        request.prazoMeses = 100; // maior que qualquer prazoMax

        when(customerRepository.findById(1L)).thenReturn(cliente("MODERADO", 1L));

        InvestmentProduct p = produtoPadrao();
        p.prazoMaxMeses = 24;

        when(productRepository.listAll()).thenReturn(List.of(p));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> simulationService.simulate(request));

        assertEquals(422, ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Nenhum produto atende aos parâmetros informados"));
    }

    // ==========================================================
    // 7. Erro inesperado em persist deve virar INTERNAL_SERVER_ERROR
    // ==========================================================

    @Test
    @DisplayName("15. Erro inesperado em persistência deve virar 500 INTERNAL_SERVER_ERROR")
    void simulate_Fail_ErroInterno() {
        InvestmentSimulationRequestDTO request = defaultRequest();

        // Esses DO são usados
        when(customerRepository.findById(1L))
                .thenReturn(cliente("ARROJADO", 1L));
        when(productRepository.findById(10L))
                .thenReturn(produtoPadrao());

        // Simula erro de banco na persistência
        doThrow(new RuntimeException("falha de banco"))
                .when(simulationRepository)
                .persist(any(InvestmentSimulation.class));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> simulationService.simulate(request));

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Erro interno ao processar a simulação"));
    }
    // ==========================================================
    // 8. listAllSimulations
    // ==========================================================

    @Test
    @DisplayName("16. listAllSimulations deve delegar para o repositório")
    void listAllSimulations_DeveDelegarParaRepositorio() {
        InvestmentSimulation s1 = new InvestmentSimulation();
        s1.id = 1L;
        InvestmentSimulation s2 = new InvestmentSimulation();
        s2.id = 2L;

        when(simulationRepository.listAll()).thenReturn(List.of(s1, s2));

        List<InvestmentSimulation> lista = simulationService.listAllSimulations();

        assertNotNull(lista);
        assertEquals(2, lista.size());
        assertEquals(1L, lista.get(0).id);
        assertEquals(2L, lista.get(1).id);
        verify(simulationRepository).listAll();
    }
}
