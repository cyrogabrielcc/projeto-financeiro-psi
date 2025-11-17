package cef.invest.ServiceTest; // Ajuste para o seu pacote

import cef.financial.domain.dto.InvestmentSimulationRequestDTO;
import cef.financial.domain.dto.InvestmentSimulationResponseDTO;
import cef.financial.domain.model.Customer;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.model.InvestmentSimulation;
import cef.financial.domain.repository.CustomerRepository;
import cef.financial.domain.repository.InvestmentProductRepository;
import cef.financial.domain.repository.InvestmentSimulationRepository;
import cef.financial.domain.service.InvestmentSimulationService;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentSimulationServiceTest {

    @Mock
    InvestmentProductRepository productRepository;
    @Mock
    InvestmentSimulationRepository simulationRepository;
    @Mock
    CustomerRepository customerRepository;

    @InjectMocks
    InvestmentSimulationService simulationService;

    @Captor
    ArgumentCaptor<InvestmentSimulation> simulationCaptor;
    @Captor
    ArgumentCaptor<Customer> customerCaptor;

    private InvestmentSimulationRequestDTO request;
    private InvestmentProduct mockProduto;
    private Customer mockCliente;

    @BeforeEach
    void setUp() {
        request = new InvestmentSimulationRequestDTO();
        request.clienteId = 1L;
        request.produtoId = 10L;
        request.valor = 1000.0;
        request.prazoMeses = 12;
        request.tipoProduto = null;

        mockCliente = new Customer();
        mockCliente.id = 1L;
        mockCliente.perfil = "ARROJADO";

        mockProduto = new InvestmentProduct();
        mockProduto.id = 10L;
        mockProduto.nome = "Produto Teste";
        mockProduto.tipo = "RENDA_FIXA";
        mockProduto.risco = "BAIXO";
        mockProduto.rentabilidadeAnual = 0.10;
        mockProduto.prazoMinMeses = 6;
        mockProduto.prazoMaxMeses = 24;
    }

    @Test
    @DisplayName("1. Deve simular com sucesso para cliente existente e produtoId fornecido")
    void simulate_Success_ClienteExiste_ProdutoIdFornecido() {
        when(customerRepository.findById(1L)).thenReturn(mockCliente);
        when(productRepository.findById(10L)).thenReturn(mockProduto);

        InvestmentSimulationResponseDTO response = simulationService.simulate(request);

        assertNotNull(response);
        assertNotNull(response.resultadoSimulacao);
        assertEquals(1100.0, response.resultadoSimulacao.valorFinal, 0.01);
        assertEquals(0.10, response.resultadoSimulacao.rentabilidadeEfetiva, 0.001);
        assertNotNull(response.produtoValidado);
        assertEquals(10L, response.produtoValidado.id);

        verify(simulationRepository).persist(simulationCaptor.capture());
        InvestmentSimulation simPersistida = simulationCaptor.getValue();
        assertEquals(1L, simPersistida.clienteId);
    }

    @Test
    @DisplayName("2. Deve criar um novo cliente se o ID fornecido não for encontrado")
    void simulate_Success_CriaNovoCliente() {
        request.clienteId = 999L;
        when(customerRepository.findById(999L)).thenReturn(null);

        doAnswer(invocation -> {
            Customer novoCliente = invocation.getArgument(0);
            novoCliente.id = 123L;
            return null;
        }).when(customerRepository).persist(any(Customer.class));

        when(productRepository.findById(10L)).thenReturn(mockProduto);

        InvestmentSimulationResponseDTO response = simulationService.simulate(request);

        assertNotNull(response);
        verify(customerRepository).persist(customerCaptor.capture());
        assertEquals("INDEFINIDO", customerCaptor.getValue().perfil);

        verify(simulationRepository).persist(simulationCaptor.capture());
        assertEquals(123L, simulationCaptor.getValue().clienteId);
    }

    @Test
    @DisplayName("3. Deve selecionar automaticamente o produto mais rentável se produtoId for nulo")
    void simulate_Success_AutoSelecaoProduto() {
        request.produtoId = null;
        request.prazoMeses = 12;
        request.tipoProduto = "RENDA_FIXA";

        InvestmentProduct prodA = new InvestmentProduct();
        prodA.id = 1L;
        prodA.nome = "Renda Fixa Baixa";
        prodA.tipo = "RENDA_FIXA";
        prodA.risco = "BAIXO";
        prodA.prazoMinMeses = 6;
        prodA.prazoMaxMeses = 24;
        prodA.rentabilidadeAnual = 0.08;

        InvestmentProduct prodB = new InvestmentProduct();
        prodB.id = 2L;
        prodB.nome = "Renda Fixa Alta";
        prodB.tipo = "RENDA_FIXA";
        prodB.risco = "MEDIO";
        prodB.prazoMinMeses = 6;
        prodB.prazoMaxMeses = 24;
        prodB.rentabilidadeAnual = 0.12;

        InvestmentProduct prodC = new InvestmentProduct();
        prodC.id = 3L;
        prodC.nome = "Renda Variavel";
        prodC.tipo = "RENDA_VARIAVEL";
        prodC.risco = "ALTO";
        prodC.prazoMinMeses = 6;
        prodC.prazoMaxMeses = 24;
        prodC.rentabilidadeAnual = 0.20;

        InvestmentProduct prodD = new InvestmentProduct();
        prodD.id = 4L;
        prodD.nome = "Renda Fixa Curta";
        prodD.tipo = "RENDA_FIXA";
        prodD.risco = "BAIXO";
        prodD.prazoMinMeses = 1;
        prodD.prazoMaxMeses = 6;
        prodD.rentabilidadeAnual = 0.10;

        when(customerRepository.findById(1L)).thenReturn(mockCliente);
        when(productRepository.listAll()).thenReturn(List.of(prodA, prodB, prodC, prodD));

        InvestmentSimulationResponseDTO response = simulationService.simulate(request);

        assertNotNull(response);
        assertNotNull(response.produtoValidado);
        assertEquals(2L, response.produtoValidado.id);
        assertEquals("Renda Fixa Alta", response.produtoValidado.nome);
    }

    @Test
    @DisplayName("4. Deve lançar BAD_REQUEST se o valor for zero ou negativo")
    void simulate_Fail_ValidarRequest_ValorInvalido() {
        request.valor = 0.0;

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> {
            simulationService.simulate(request);
        });

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("valor deve ser informado e maior que zero"));
    }

    @Test
    @DisplayName("5. Deve lançar NOT_FOUND se o produtoId fornecido não existir")
    void simulate_Fail_ProdutoNaoEncontrado() {
        request.produtoId = 999L;
        when(customerRepository.findById(1L)).thenReturn(mockCliente);
        when(productRepository.findById(999L)).thenReturn(null);

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> {
            simulationService.simulate(request);
        });

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Produto não encontrado: 999"));
    }

    @Test
    @DisplayName("6. Deve lançar BAD_REQUEST se o produto não atender ao prazo")
    void simulate_Fail_PrazoNaoAtendido() {
        request.prazoMeses = 36;
        when(customerRepository.findById(1L)).thenReturn(mockCliente);
        when(productRepository.findById(10L)).thenReturn(mockProduto);

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> {
            simulationService.simulate(request);
        });

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("não atende ao prazo solicitado (36 meses)"));
    }

    @Test
    @DisplayName("7. Deve lançar UNPROCESSABLE_ENTITY se nenhum produto for elegível na auto-seleção")
    void simulate_Fail_NenhumProdutoElegivel() {
        request.produtoId = null;
        request.prazoMeses = 100;

        InvestmentProduct prodA = new InvestmentProduct();
        prodA.id = 1L;
        prodA.nome = "Produto Curto";
        prodA.tipo = "RENDA_FIXA";
        prodA.risco = "BAIXO";
        prodA.prazoMinMeses = 6;
        prodA.prazoMaxMeses = 24;
        prodA.rentabilidadeAnual = 0.08;

        when(customerRepository.findById(1L)).thenReturn(mockCliente);
        when(productRepository.listAll()).thenReturn(List.of(prodA));

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> {
            simulationService.simulate(request);
        });

        assertEquals(422, ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Nenhum produto atende aos parâmetros informados"));
    }

    @Test
    @DisplayName("8. Deve lançar BAD_REQUEST se a rentabilidade do produto for nula")
    void simulate_Fail_RentabilidadeNula() {
        mockProduto.rentabilidadeAnual = null;
        when(customerRepository.findById(1L)).thenReturn(mockCliente);
        when(productRepository.findById(10L)).thenReturn(mockProduto);

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> {
            simulationService.simulate(request);
        });

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Rentabilidade anual não definida"));
    }

    // ==========================================================
    // 9, 10, 11 – NOVOS TESTES PARA AUMENTAR COBERTURA
    // ==========================================================

    @Test
    @DisplayName("9. Deve simular com sucesso quando tipoProduto é preenchido junto com produtoId")
    void simulate_Success_ProdutoIdETipoPreenchidos() {
        request.tipoProduto = "RENDA_FIXA";

        when(customerRepository.findById(1L)).thenReturn(mockCliente);
        when(productRepository.findById(10L)).thenReturn(mockProduto);

        InvestmentSimulationResponseDTO response = simulationService.simulate(request);

        assertNotNull(response);
        assertNotNull(response.resultadoSimulacao);
        assertNotNull(response.produtoValidado);
        assertEquals(10L, response.produtoValidado.id);

        verify(simulationRepository, atLeastOnce()).persist(any(InvestmentSimulation.class));
    }

    @Test
    @DisplayName("10. Auto-seleção de produto deve funcionar mesmo sem tipoProduto definido")
    void simulate_Success_AutoSelecao_SemTipoProduto() {
        request.produtoId = null;
        request.tipoProduto = null;
        request.prazoMeses = 12;

        InvestmentProduct prodUnico = new InvestmentProduct();
        prodUnico.id = 50L;
        prodUnico.nome = "Produto Único";
        prodUnico.tipo = "RENDA_FIXA";
        prodUnico.risco = "MEDIO";
        prodUnico.prazoMinMeses = 6;
        prodUnico.prazoMaxMeses = 36;
        prodUnico.rentabilidadeAnual = 0.11;

        when(customerRepository.findById(1L)).thenReturn(mockCliente);
        when(productRepository.listAll()).thenReturn(List.of(prodUnico));

        InvestmentSimulationResponseDTO response = simulationService.simulate(request);

        assertNotNull(response);
        assertNotNull(response.produtoValidado);
        assertEquals(50L, response.produtoValidado.id);
    }

    @Test
    @DisplayName("11. Cliente existente não deve ser recriado")
    void simulate_Success_ClienteExistente_NaoCriaNovo() {
        when(customerRepository.findById(1L)).thenReturn(mockCliente);
        when(productRepository.findById(10L)).thenReturn(mockProduto);

        InvestmentSimulationResponseDTO response = simulationService.simulate(request);

        assertNotNull(response);
        verify(customerRepository, never()).persist(any(Customer.class));
        verify(simulationRepository, times(1)).persist(any(InvestmentSimulation.class));
    }
}
