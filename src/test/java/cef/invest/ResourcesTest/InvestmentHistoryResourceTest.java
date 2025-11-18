package cef.invest.ResourcesTest;

import cef.financial.api.resources.InvestmentHistoryResource;
import cef.financial.domain.dto.InvestmentHistoryResponseDTO;
import cef.financial.domain.model.Customer;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.model.InvestmentSimulation;
import cef.financial.domain.repository.CustomerRepository;
import cef.financial.domain.repository.InvestmentSimulationRepository;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentHistoryResourceTest {

    @Mock
    InvestmentSimulationRepository simulationRepository;

    @Mock
    CustomerRepository customerRepository;

    private InvestmentHistoryResource resource;

    @BeforeEach
    void setUp() {
        // bate com o construtor existente no resource
        resource = new InvestmentHistoryResource(simulationRepository);
    }

    // ========================================================
    // 1. TESTES DE ANOTAÇÕES
    // ========================================================

    @Test
    @DisplayName("A classe deve ter anotações corretas")
    void testClassAnnotations() {
        assertTrue(InvestmentHistoryResource.class.isAnnotationPresent(Path.class));
        assertEquals("/investimentos",
                InvestmentHistoryResource.class.getAnnotation(Path.class).value());

        assertTrue(InvestmentHistoryResource.class.isAnnotationPresent(Consumes.class));
        assertEquals(MediaType.APPLICATION_JSON,
                InvestmentHistoryResource.class.getAnnotation(Consumes.class).value()[0]);

        assertTrue(InvestmentHistoryResource.class.isAnnotationPresent(Produces.class));
        assertEquals(MediaType.APPLICATION_JSON,
                InvestmentHistoryResource.class.getAnnotation(Produces.class).value()[0]);

        assertTrue(InvestmentHistoryResource.class.isAnnotationPresent(Authenticated.class));
    }

    @Test
    @DisplayName("O método deve ter as anotações corretas")
    void testMethodAnnotations() throws NoSuchMethodException {
        var method = InvestmentHistoryResource.class.getMethod("historicoInvestimentos", Long.class);

        assertTrue(method.isAnnotationPresent(GET.class));
        assertTrue(method.isAnnotationPresent(RolesAllowed.class));
        assertTrue(method.isAnnotationPresent(Path.class));

        RolesAllowed roles = method.getAnnotation(RolesAllowed.class);
        assertArrayEquals(new String[]{"user", "admin"}, roles.value());

        assertEquals("/{clienteId}", method.getAnnotation(Path.class).value());
    }

    // ========================================================
    // 2. TESTES DE COMPORTAMENTO
    // ========================================================

    @Test
    @DisplayName("Deve lançar NotFoundException quando não houver simulações para o cliente")
    void testClienteSemSimulacoes() {
        Long clienteId = 10L;

        // Sem simulações para o cliente
        when(simulationRepository.list("clienteId", clienteId))
                .thenReturn(List.of());

        // Agora o comportamento esperado é exceção, não lista vazia
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> resource.historicoInvestimentos(clienteId)
        );

        assertEquals("Cliente não existente na base", ex.getMessage());

        verify(simulationRepository, times(1))
                .list("clienteId", clienteId);
    }

    @Test
    @DisplayName("Deve mapear corretamente uma simulação para DTO")
    void testHistoricoInvestimentos_MapeamentoReal() {
        Long clienteId = 20L;
        LocalDate data = LocalDate.of(2024, 1, 15);

        InvestmentSimulation s = simulation(
                1L,
                "RF",
                1500.0,
                1680.0,
                data
        );

        when(simulationRepository.list("clienteId", clienteId))
                .thenReturn(List.of(s));

        List<InvestmentHistoryResponseDTO> result =
                resource.historicoInvestimentos(clienteId);

        assertNotNull(result);
        assertEquals(1, result.size());

        InvestmentHistoryResponseDTO dto = result.get(0);

        assertEquals(1L, dto.id);
        assertEquals("RF", dto.tipo);
        assertEquals(1500.0, dto.valor);
        // (1680 - 1500) / 1500 = 0.12
        assertEquals(0.12, dto.rentabilidade, 0.0000001);
        assertEquals(data, dto.data);

        verify(simulationRepository, times(1))
                .list("clienteId", clienteId);
    }

    @Test
    @DisplayName("Deve suportar grande volume de dados e retornar lista com 500 itens")
    void testHistoricoInvestimentos_ListGrande() {
        Long clienteId = 7L;

        List<InvestmentSimulation> grandeLista =
                java.util.stream.IntStream.range(0, 500)
                        .mapToObj(i -> simulation(
                                (long) i,
                                "TIPO",
                                100 + i,
                                120 + i,
                                LocalDate.now()
                        ))
                        .toList();

        when(simulationRepository.list("clienteId", clienteId))
                .thenReturn(grandeLista);

        List<InvestmentHistoryResponseDTO> result =
                resource.historicoInvestimentos(clienteId);

        assertEquals(500, result.size());
        verify(simulationRepository, times(1))
                .list("clienteId", clienteId);
    }

    @Test
    @DisplayName("A ordem dos itens deve ser preservada")
    void testOrdemPreservada() {
        Long clienteId = 30L;
        LocalDate hoje = LocalDate.now();

        InvestmentSimulation s1 = simulation(1L, "A", 100, 110, hoje);
        InvestmentSimulation s2 = simulation(2L, "B", 200, 220, hoje);

        when(simulationRepository.list("clienteId", clienteId))
                .thenReturn(List.of(s1, s2));

        List<InvestmentHistoryResponseDTO> result =
                resource.historicoInvestimentos(clienteId);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id);
        assertEquals(2L, result.get(1).id);
    }

    // ========================================================
    // HELPERS
    // ========================================================

    private InvestmentSimulation simulation(Long id,
                                            String tipoProduto,
                                            double valorInvestido,
                                            double valorFinal,
                                            LocalDate data) {

        InvestmentSimulation s = new InvestmentSimulation();
        s.id = id;
        s.valorInvestido = valorInvestido;
        s.valorFinal = valorFinal;
        s.dataSimulacao = data.atStartOfDay().atOffset(ZoneOffset.UTC);

        InvestmentProduct p = new InvestmentProduct();
        p.tipo = tipoProduto;
        s.produto = p;

        return s;
    }
}
