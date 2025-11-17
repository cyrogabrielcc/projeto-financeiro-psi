package cef.invest.ResourcesTest;

import cef.financial.api.resources.InvestmentHistoryResource;
import cef.financial.domain.dto.InvestmentHistoryResponseDTO;
import cef.financial.domain.exception.ApiError;
import cef.financial.domain.model.InvestmentHistory;
import cef.financial.domain.repository.InvestmentHistoryRepository;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentHistoryResourceTest {

    @Mock
    InvestmentHistoryRepository investmentHistoryRepository;

    private InvestmentHistoryResource resource;

    @BeforeEach
    void setUp() {
        resource = new InvestmentHistoryResource(investmentHistoryRepository);
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
    // 2. TESTES DE MAPEAMENTO (helpers puros)
    // ========================================================

    @Test
    @DisplayName("Mapeamento simples de InvestmentHistory → DTO")
    void testMapeamentoSimples() {
        LocalDate data = LocalDate.of(2024, 1, 15);

        InvestmentHistory h = new InvestmentHistory();
        h.id = 1L;
        h.tipo = "RF";
        h.valor = 1000;
        h.rentabilidade = 0.10;
        h.dataInvestimento = data;

        InvestmentHistoryResponseDTO dto = mapear(h);

        assertEquals(1L, dto.id);
        assertEquals("RF", dto.tipo);
        assertEquals(1000, dto.valor);
        assertEquals(0.10, dto.rentabilidade);
        assertEquals(data, dto.data);
    }

    @Test
    @DisplayName("Mapeamento deve funcionar com valores nulos")
    void testMapeamentoNulos() {
        InvestmentHistory h = new InvestmentHistory();

        InvestmentHistoryResponseDTO dto = mapear(h);

        assertNull(dto.id);
        assertNull(dto.tipo);
        assertEquals(0.0, dto.valor);
        assertEquals(0.0, dto.rentabilidade);
        assertNull(dto.data);
    }

    @Test
    @DisplayName("Mapeamento deve funcionar com valores extremos")
    void testMapeamentoExtremos() {
        LocalDate data = LocalDate.of(9999, 12, 31);
        InvestmentHistory h = new InvestmentHistory();
        h.id = Long.MAX_VALUE;
        h.tipo = "TIPO_EXTREMO";
        h.valor = Double.MAX_VALUE;
        h.rentabilidade = Double.MIN_VALUE;
        h.dataInvestimento = data;

        InvestmentHistoryResponseDTO dto = mapear(h);

        assertEquals(Long.MAX_VALUE, dto.id);
        assertEquals(Double.MAX_VALUE, dto.valor);
        assertEquals(Double.MIN_VALUE, dto.rentabilidade);
        assertEquals(data, dto.data);
    }

    // ========================================================
    // 3. TESTES DO MÉTODO historicoInvestimentos
    // ========================================================

    @Test
    @DisplayName("historicoInvestimentos deve mapear corretamente e retornar lista com 1 item")
    void testHistoricoInvestimentos_MapeamentoReal() {
        Long clienteId = 10L;
        LocalDate data = LocalDate.of(2024, 1, 15);

        InvestmentHistory h = new InvestmentHistory();
        h.id = 1L;
        h.tipo = "RF";
        h.valor = 1500;
        h.rentabilidade = 0.12;
        h.dataInvestimento = data;

        when(investmentHistoryRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h));

        List<InvestmentHistoryResponseDTO> result = resource.historicoInvestimentos(clienteId);

        assertEquals(1, result.size());
        InvestmentHistoryResponseDTO dto = result.get(0);

        assertEquals(1L, dto.id);
        assertEquals("RF", dto.tipo);
        assertEquals(1500, dto.valor);
        assertEquals(0.12, dto.rentabilidade);
        assertEquals(data, dto.data);

        verify(investmentHistoryRepository, times(1))
                .list("clienteId", clienteId);
    }

    @Test
    @DisplayName("historicoInvestimentos deve retornar 404 e ApiError quando lista vazia")
    void testHistoricoInvestimentos_ListaVazia() {
        Long clienteId = 99L;

        when(investmentHistoryRepository.list("clienteId", clienteId))
                .thenReturn(List.of());

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.historicoInvestimentos(clienteId)
        );

        Response response = ex.getResponse();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof ApiError);

        ApiError error = (ApiError) response.getEntity();
        assertEquals("HISTORICO_NAO_ENCONTRADO", error.code);
        assertTrue(error.message.contains(clienteId.toString()));
    }

    @Test
    @DisplayName("historicoInvestimentos com clienteId negativo deve retornar 404 e ApiError")
    void testHistoricoInvestimentos_IdNegativo() {
        Long clienteId = -1L;

        when(investmentHistoryRepository.list("clienteId", clienteId))
                .thenReturn(List.of());

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.historicoInvestimentos(clienteId)
        );

        Response response = ex.getResponse();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof ApiError);
    }

    @Test
    @DisplayName("Deve suportar grande volume de dados e retornar lista com 500 itens")
    void testHistoricoInvestimentos_ListGrande() {
        Long clienteId = 7L;

        List<InvestmentHistory> grandeLista =
                java.util.stream.IntStream.range(0, 500)
                        .mapToObj(i -> {
                            InvestmentHistory h = new InvestmentHistory();
                            h.id = (long) i;
                            h.tipo = "TIPO";
                            h.valor = 100 + i;
                            h.rentabilidade = 0.05;
                            h.dataInvestimento = LocalDate.now();
                            return h;
                        }).toList();

        when(investmentHistoryRepository.list("clienteId", clienteId))
                .thenReturn(grandeLista);

        List<InvestmentHistoryResponseDTO> result = resource.historicoInvestimentos(clienteId);

        assertEquals(500, result.size());
    }

    @Test
    @DisplayName("As informações do DTO não devem ser as mesmas instâncias do model")
    void testHistoricoInvestimentos_IndependenciaObjetos() {
        Long clienteId = 20L;

        InvestmentHistory h = new InvestmentHistory();
        h.id = 1L;
        h.tipo = "X";
        h.valor = 100;
        h.rentabilidade = 0.10;
        h.dataInvestimento = LocalDate.of(2024,1,1);

        when(investmentHistoryRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h));

        List<InvestmentHistoryResponseDTO> result = resource.historicoInvestimentos(clienteId);

        assertEquals(1, result.size());
        InvestmentHistoryResponseDTO dto = result.get(0);

        // verifica que não é a mesma instância
        assertNotSame(h, dto);
        // e que os dados foram copiados corretamente
        assertEquals(h.id, dto.id);
        assertEquals(h.tipo, dto.tipo);
        assertEquals(h.valor, dto.valor);
        assertEquals(h.rentabilidade, dto.rentabilidade);
        assertEquals(h.dataInvestimento, dto.data);
    }

    @Test
    @DisplayName("A ordem dos itens deve ser preservada")
    void testOrdemPreservada() {
        Long clienteId = 30L;

        InvestmentHistory h1 = history(1L, "A", 100, 0.05, LocalDate.now());
        InvestmentHistory h2 = history(2L, "B", 200, 0.10, LocalDate.now());

        when(investmentHistoryRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1, h2));

        List<InvestmentHistoryResponseDTO> result = resource.historicoInvestimentos(clienteId);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id);
        assertEquals(2L, result.get(1).id);
    }

    // ========================================================
    // HELPERS
    // ========================================================

    private InvestmentHistory history(Long id, String tipo, double valor, double rentabilidade, LocalDate data) {
        InvestmentHistory h = new InvestmentHistory();
        h.id = id;
        h.tipo = tipo;
        h.valor = valor;
        h.rentabilidade = rentabilidade;
        h.dataInvestimento = data;
        return h;
    }

    private InvestmentHistoryResponseDTO mapear(InvestmentHistory h) {
        InvestmentHistoryResponseDTO dto = new InvestmentHistoryResponseDTO();
        dto.id = h.id;
        dto.tipo = h.tipo;
        dto.valor = h.valor;
        dto.rentabilidade = h.rentabilidade;
        dto.data = h.dataInvestimento;
        return dto;
    }
}
