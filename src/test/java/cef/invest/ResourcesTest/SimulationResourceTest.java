package cef.invest.ResourcesTest;

import cef.financial.api.resources.SimulationResource;
import cef.financial.domain.dto.InvestmentSimulationRequestDTO;
import cef.financial.domain.dto.InvestmentSimulationResponseDTO;
import cef.financial.domain.dto.SimulationByProductDayResponseDTO;
import cef.financial.domain.dto.SimulationHistoryResponseDTO;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.model.InvestmentSimulation;
import cef.financial.domain.service.InvestmentSimulationService;
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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulationResourceTest {

    @Mock
    InvestmentSimulationService simulationService;

    SimulationResource resource;

    @BeforeEach
    void setUp() {
        // usa o construtor que recebe o service (que você já definiu no Resource)
        resource = new SimulationResource(simulationService);
    }

    // ----------------------------- ANOTAÇÕES -----------------------------

    @Test
    void testClassAnnotations() {
        assertTrue(SimulationResource.class.isAnnotationPresent(Path.class));
        assertEquals("", SimulationResource.class.getAnnotation(Path.class).value());

        assertTrue(SimulationResource.class.isAnnotationPresent(Consumes.class));
        assertEquals(MediaType.APPLICATION_JSON,
                SimulationResource.class.getAnnotation(Consumes.class).value()[0]);

        assertTrue(SimulationResource.class.isAnnotationPresent(Produces.class));
        assertEquals(MediaType.APPLICATION_JSON,
                SimulationResource.class.getAnnotation(Produces.class).value()[0]);

        assertTrue(SimulationResource.class.isAnnotationPresent(Authenticated.class));
    }

    @Test
    void testSimularInvestimentoAnnotations() throws Exception {
        var dtoClass = InvestmentSimulationRequestDTO.class;
        var method = SimulationResource.class.getMethod("simularInvestimento", dtoClass);

        assertTrue(method.isAnnotationPresent(POST.class));
        assertTrue(method.isAnnotationPresent(Path.class));
        assertEquals("/simular-investimento", method.getAnnotation(Path.class).value());

        RolesAllowed roles = method.getAnnotation(RolesAllowed.class);
        assertArrayEquals(new String[]{"user", "admin"}, roles.value());
    }

    @Test
    void testListarSimulacoesAnnotations() throws Exception {
        var method = SimulationResource.class.getMethod("listarSimulacoes");

        assertTrue(method.isAnnotationPresent(GET.class));
        assertEquals("/simulacoes", method.getAnnotation(Path.class).value());
    }

    @Test
    void testSimulacoesPorProdutoDiaAnnotations() throws Exception {
        var method = SimulationResource.class.getMethod("simulacoesPorProdutoDia");

        assertTrue(method.isAnnotationPresent(GET.class));
        assertEquals("/simulacoes/por-produto-dia", method.getAnnotation(Path.class).value());
    }

    // ----------------------------- TESTE DO POST REAL -----------------------------

    @Test
    void testSimularInvestimento_ChamadaReal() {
        // cria o request
        InvestmentSimulationRequestDTO req = new InvestmentSimulationRequestDTO();
        req.clienteId = 1L;
        req.tipoProduto = "CDB";
        req.valor = 1000.0;
        req.prazoMeses = 12;

        // cria o response simulado
        InvestmentSimulationResponseDTO resp = new InvestmentSimulationResponseDTO();
        resp.resultadoSimulacao = new InvestmentSimulationResponseDTO.ResultadoSimulacao();
        resp.resultadoSimulacao.valorFinal = 2000.00;

        when(simulationService.simulate(req)).thenReturn(resp);

        // chama o método real
        Response response = resource.simularInvestimento(req);

        assertNotNull(response);
        assertEquals(200, response.getStatus());

        InvestmentSimulationResponseDTO corpo =
                (InvestmentSimulationResponseDTO) response.getEntity();

        assertNotNull(corpo.resultadoSimulacao);
        assertEquals(2000.00, corpo.resultadoSimulacao.valorFinal);

        verify(simulationService).simulate(req);
    }

    // ----------------------------- TESTES DO listAllSimulations -----------------------------

    @Test
    void testListarSimulacoes_MapeamentoCompleto() {

        InvestmentProduct prod = new InvestmentProduct();
        prod.id = 1L; // acompanha seu padrão de IDs fixos
        prod.nome = "Produto Teste";

        InvestmentSimulation sim = new InvestmentSimulation();
        sim.id = 1L;
        sim.clienteId = 50L;
        sim.produto = prod;
        sim.valorInvestido = 1000;
        sim.valorFinal = 1200;
        sim.prazoMeses = 12;
        sim.dataSimulacao = OffsetDateTime.now(ZoneOffset.UTC);

        when(simulationService.listAllSimulations()).thenReturn(List.of(sim));

        List<SimulationHistoryResponseDTO> result = resource.listarSimulacoes();

        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).clienteId);
        verify(simulationService).listAllSimulations();
    }

    @Test
    void testListarSimulacoes_Vazio() {
        when(simulationService.listAllSimulations()).thenReturn(List.of());

        List<SimulationHistoryResponseDTO> result = resource.listarSimulacoes();

        assertTrue(result.isEmpty());
    }

    // ----------------------------- TESTE DO AGRUPAMENTO REAL -----------------------------

    @Test
    void testSimulacoesPorProdutoDia_MapeamentoCompleto() {

        InvestmentProduct a = new InvestmentProduct();
        a.id = 1L;
        a.nome = "A";

        InvestmentProduct b = new InvestmentProduct();
        b.id = 2L;
        b.nome = "B";

        OffsetDateTime d1 = OffsetDateTime.of(2025,11,1,10,0,0,0,ZoneOffset.UTC);
        OffsetDateTime d2 = OffsetDateTime.of(2025,11,2,10,0,0,0,ZoneOffset.UTC);

        InvestmentSimulation s1 = criarSim(a, 100, 150, d1);
        InvestmentSimulation s2 = criarSim(a, 200, 300, d1);
        InvestmentSimulation s3 = criarSim(a, 300, 450, d2);
        InvestmentSimulation s4 = criarSim(b, 500, 550, d1);

        when(simulationService.listAllSimulations())
                .thenReturn(List.of(s1, s2, s3, s4));

        List<SimulationByProductDayResponseDTO> result =
                resource.simulacoesPorProdutoDia();

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(r ->
                r.produto.equals("A") && r.data.equals(d1.toLocalDate()) && r.quantidadeSimulacoes == 2
        ));
        assertTrue(result.stream().anyMatch(r ->
                r.produto.equals("B") && r.data.equals(d1.toLocalDate()) && r.quantidadeSimulacoes == 1
        ));
    }

    @Test
    void testSimulacoesPorProdutoDia_Vazio() {
        when(simulationService.listAllSimulations()).thenReturn(List.of());

        List<SimulationByProductDayResponseDTO> result =
                resource.simulacoesPorProdutoDia();

        assertTrue(result.isEmpty());
    }

    // ----------------------------- COBERTURA EXTRA -----------------------------

    @Test
    void testDTOsModelosInstanciaveis() {
        assertNotNull(new InvestmentSimulation());
        assertNotNull(new InvestmentProduct());
        assertNotNull(new SimulationHistoryResponseDTO());
        assertNotNull(new SimulationByProductDayResponseDTO());
        assertNotNull(new InvestmentSimulationRequestDTO());
        assertNotNull(new InvestmentSimulationResponseDTO());
    }

    // ----------------------------- MÉTODOS AUXILIARES -----------------------------

    private InvestmentSimulation criarSim(InvestmentProduct p, double vi, double vf, OffsetDateTime d) {
        InvestmentSimulation s = new InvestmentSimulation();
        s.produto = p;
        s.valorInvestido = vi;
        s.valorFinal = vf;
        s.dataSimulacao = d;
        return s;
    }
}
