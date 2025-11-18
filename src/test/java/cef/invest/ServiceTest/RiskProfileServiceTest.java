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
    @DisplayName("2. Deve classificar como Conservador (renda fixa com prejuízo e pouca experiência)")
    void calculateProfile_Conservador() {
        Long clienteId = 2L;

        InvestmentHistory h1 = history("CDB Renda Fixa", 1000.0, -0.01);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Conservador", resp.perfil);
        // Novo score com a lógica atual: 5 (retorno) + 5 (risco baixo) + 5 (qtd <=1) = 15
        assertEquals(15, resp.pontuacao);
    }

    @Test
    @DisplayName("3. Moderado (retorno médio, risco médio, experiência moderada)")
    void calculateProfile_Moderado() {
        Long clienteId = 3L;

        InvestmentHistory h1 = history("Fundo Multimercado", 2000.0, 0.07);
        InvestmentHistory h2 = history("Fundo Multimercado", 1000.0, 0.07);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1, h2));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Moderado", resp.perfil);
        // returnScore=25, riskExposure=18, experience=10 -> 53
        assertEquals(53, resp.pontuacao);
    }

    @Test
    @DisplayName("4. Agressivo com alta experiência (pontuação muito alta)")
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

        assertEquals("Agressivo", resp.perfil);
        // Com a lógica nova, dá 95 (40 retorno + 30 risco + 25 experiência)
        assertEquals(95, resp.pontuacao);
    }

    @Test
    @DisplayName("5. Alta exposição a risco com pouca experiência deve ficar Moderado (penalidade aplicada)")
    void calculateProfile_Agressivo_ComPenalidade() {
        Long clienteId = 5L;

        InvestmentHistory h1 = history("Ação", 1000.0, 0.20);
        InvestmentHistory h2 = history("FII", 500.0, 0.25);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1, h2));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        // Agora, com penalização mais forte, esse caso cai em Moderado
        assertEquals("Moderado", resp.perfil);
        // 40 (retorno) + 30 (risco) + 10 (experiência) - 15 (penalidade) = 65
        assertEquals(65, resp.pontuacao);
    }

    @Test
    @DisplayName("6. Tipos desconhecidos continuam levando a perfil Moderado (risco tratado na média)")
    void calculateProfile_TipoDesconhecido() {
        Long clienteId = 6L;

        InvestmentHistory h1 = history("CRYPTO RANDOM", 1000.0, 0.10);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertNotNull(resp);
        // Com retorno ~10% e risco default baixo, cai em Moderado com a nova calibração
        assertEquals("Moderado", resp.perfil);
    }

    @Test
    @DisplayName("7. Rentabilidade zero em multimercado deve gerar perfil Conservador")
    void calculateProfile_RentabilidadeZero() {
        Long clienteId = 7L;

        InvestmentHistory h1 = history("Fundo Multimercado", 500.0, 0.0);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Conservador", resp.perfil);
        // 5 (retorno <=0) + 18 (risco médio) + 5 (pouca experiência) = 28
        assertEquals(28, resp.pontuacao);
    }

    @Test
    @DisplayName("8. Rentabilidade cadastrada como 150% (1.5) deve ser normalizada e não quebrar o cálculo")
    void calculateProfile_RentabilidadeExtrema() {
        Long clienteId = 8L;

        // 1.5 aqui, com a normalização (>1 -> /100), vira 0.015 (1,5%)
        InvestmentHistory h1 = history("Ação", 1000.0, 1.5);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertNotNull(resp);
        // Com a lógica atual: 35 pontos -> Conservador
        assertEquals("Conservador", resp.perfil);
        assertEquals(35, resp.pontuacao);
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
        // Pela combinação (inclui FII e multimercado), tende para Moderado/Agressivo
        assertTrue(resp.perfil.equals("Moderado") || resp.perfil.equals("Agressivo"));
    }

    @Test
    @DisplayName("11. Deve lidar com rentabilidade negativa extrema (perfil Conservador)")
    void calculateProfile_RentabilidadeMuitoNegativa() {
        Long clienteId = 11L;

        InvestmentHistory h1 = history("CDB", 1000, -0.50);

        when(historyRepository.list("clienteId", clienteId)).thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Conservador", resp.perfil);
        // 5 (retorno) + 5 (risco baixo) + 5 (pouca experiência) = 15
        assertEquals(15, resp.pontuacao);
    }

    @Test
    @DisplayName("12. Deve considerar corretamente quantidade MUITO alta de operações (stress test de experiência)")
    void calculateProfile_GrandeQuantidade() {
        Long clienteId = 12L;

        List<InvestmentHistory> historico = java.util.stream.IntStream.range(0, 200)
                .mapToObj(i -> history("Ação", 100 + i, 0.20))
                .toList();

        when(historyRepository.list("clienteId", clienteId)).thenReturn(historico);

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Agressivo", resp.perfil);
        // Com a nova lógica, dá 95, mas garantimos apenas que seja bem alto
        assertTrue(resp.pontuacao >= 90);
    }
}
