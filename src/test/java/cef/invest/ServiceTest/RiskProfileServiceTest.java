package cef.invest.ServiceTest;

import cef.financial.domain.dto.RiskProfileResponseDTO;
import cef.financial.domain.model.InvestmentHistory;
import cef.financial.domain.repository.InvestmentHistoryRepository;
import cef.financial.domain.repository.CustomerRepository;
import cef.financial.domain.service.RiskProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskProfileServiceTest {

    @Mock
    InvestmentHistoryRepository historyRepository;

    @Mock
    CustomerRepository customerRepository;

    @InjectMocks
    RiskProfileService riskProfileService;

    // Helper
    private InvestmentHistory history(String tipo, double valor, double rentabilidade) {
        InvestmentHistory h = new InvestmentHistory();
        h.tipo = tipo;
        h.valor = valor;
        h.rentabilidade = rentabilidade;
        return h;
    }

    // ========== TESTES ==========

    @Test
    @DisplayName("1. Deve retornar perfil 'Indefinido' quando não houver histórico")
    void calculateProfile_SemHistorico() {
        Long clienteId = 1L;

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of());

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertNotNull(resp);
        assertEquals(clienteId, resp.clienteId);
        assertEquals("Indefinido", resp.perfil);
        assertEquals(0, resp.pontuacao);
        assertTrue(resp.descricao.toLowerCase().contains("sem histórico"));
    }

    @Test
    @DisplayName("2. Conservador: renda fixa com prejuízo e pouca experiência")
    void calculateProfile_Conservador() {
        Long clienteId = 2L;

        InvestmentHistory h1 = history("CDB Renda Fixa", 1000.0, -0.01);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Conservador", resp.perfil);
        // avgReturn = -0.01 → returnScore=5, risco baixo=2, pouca experiência=3 => 10
        assertEquals(10, resp.pontuacao);
    }

    @Test
    @DisplayName("3. Conservador com retorno moderado em multimercado")
    void calculateProfile_Moderado() {
        Long clienteId = 3L;

        InvestmentHistory h1 = history("Fundo Multimercado", 2000.0, 0.07);
        InvestmentHistory h2 = history("Fundo Multimercado", 1000.0, 0.07);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1, h2));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        // avgReturn = 7% → returnScore=25; risco médio=18; qtd=2 → exp=5 → total=48
        assertEquals("Conservador", resp.perfil);
        assertEquals(48, resp.pontuacao);
    }

    @Test
    @DisplayName("4. Moderado com alta experiência e alta exposição a risco")
    void calculateProfile_Agressivo_SemPenalidade() {
        Long clienteId = 4L;

        List<InvestmentHistory> historico = List.of(
                history("Ação", 1000.0, 0.20),
                history("FII", 1500.0, 0.18),
                history("Ação", 2000.0, 0.22),
                history("FII", 500.0, 0.19),
                history("Ação", 800.0, 0.21),
                history("FII", 700.0, 0.17),
                history("Ação", 900.0, 0.23),
                history("Ação", 600.0, 0.20),
                history("FII", 400.0, 0.19),
                history("Ação", 300.0, 0.18),
                history("FII", 300.0, 0.19)
        );

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(historico);

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        // maxRiskLevel=3 (Ação/FII), avgReturn > 0.12 → returnScore=40; risco=30; exp (qtd>10)=20 → 90
        assertEquals("Moderado", resp.perfil);
        assertEquals(90, resp.pontuacao);
    }

    @Test
    @DisplayName("5. Alta exposição a risco com pouca experiência deve ficar Conservador (penalidade aplicada)")
    void calculateProfile_Agressivo_ComPenalidade() {
        Long clienteId = 5L;

        InvestmentHistory h1 = history("Ação", 1000.0, 0.20);
        InvestmentHistory h2 = history("FII", 500.0, 0.25);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1, h2));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        // avgReturn > 12% → 40; risco=30; exp (qtd=2)=5; subtotal=75; penalidade -15 => 60
        assertEquals("Conservador", resp.perfil);
        assertEquals(60, resp.pontuacao);
    }

    @Test
    @DisplayName("6. Tipo desconhecido cai em Conservador com lógica atual")
    void calculateProfile_TipoDesconhecido() {
        Long clienteId = 6L;

        InvestmentHistory h1 = history("CRYPTO RANDOM", 1000.0, 0.10);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertNotNull(resp);
        // tipo não bate em nada → risco baixo (1); avgReturn 10% → returnScore=35; risco=2; exp=3 => 40
        assertEquals("Conservador", resp.perfil);
    }

    @Test
    @DisplayName("7. Rentabilidade zero em multimercado gera perfil Conservador")
    void calculateProfile_RentabilidadeZero() {
        Long clienteId = 7L;

        InvestmentHistory h1 = history("Fundo Multimercado", 500.0, 0.0);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        // avgReturn=0 → 5; risco médio=18; exp=3 => 26
        assertEquals("Conservador", resp.perfil);
        assertEquals(26, resp.pontuacao);
    }

    @Test
    @DisplayName("8. Rentabilidade cadastrada como 150% (1.5) é normalizada e não quebra o cálculo")
    void calculateProfile_RentabilidadeExtrema() {
        Long clienteId = 8L;

        // 1.5 → normaliza para 0.015
        InvestmentHistory h1 = history("Ação", 1000.0, 1.5);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertNotNull(resp);
        // avgReturn ~1.5% → 15; risco=30; exp=3; subtotal=48; penalidade -15 => 33
        assertEquals("Conservador", resp.perfil);
        assertEquals(33, resp.pontuacao);
    }

    @Test
    @DisplayName("9. Valores zero não devem quebrar o cálculo")
    void calculateProfile_ValoresZero() {
        Long clienteId = 9L;

        InvestmentHistory h1 = history("CDB", 0.0, 0.05);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        assertDoesNotThrow(() -> riskProfileService.calculateProfile(clienteId));
    }

    @Test
    @DisplayName("10. Histórico misto com vários tipos de risco")
    void calculateProfile_Misto() {
        Long clienteId = 10L;

        List<InvestmentHistory> historico = List.of(
                history("CDB", 1000, 0.02),
                history("FII", 500, 0.18),
                history("Fundo Multimercado", 700, 0.07)
        );

        when(historyRepository.list("clienteId", clienteId)).thenReturn(historico);

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertNotNull(resp);
        // combinação leva para score ~= 70 → Moderado
        assertEquals("Moderado", resp.perfil);
    }

    @Test
    @DisplayName("11. Rentabilidade muito negativa mantém perfil Conservador")
    void calculateProfile_RentabilidadeMuitoNegativa() {
        Long clienteId = 11L;

        InvestmentHistory h1 = history("CDB", 1000, -0.50);

        when(historyRepository.list("clienteId", clienteId)).thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        // avgReturn << 0 → 5; risco=2; exp=3 => 10
        assertEquals("Conservador", resp.perfil);
        assertEquals(10, resp.pontuacao);
    }

    @Test
    @DisplayName("12. Grande quantidade de operações de alto risco gera score muito alto (Moderado limite superior)")
    void calculateProfile_GrandeQuantidade() {
        Long clienteId = 12L;

        List<InvestmentHistory> historico = java.util.stream.IntStream.range(0, 200)
                .mapToObj(i -> history("Ação", 100 + i, 0.20))
                .toList();

        when(historyRepository.list("clienteId", clienteId)).thenReturn(historico);

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Moderado", resp.perfil);
        assertEquals(90, resp.pontuacao);
    }
}
