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

    // NOVO: mock para a nova dependência do serviço
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

    // ========== TESTES EXISTENTES (Mantidos) ==========

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
    @DisplayName("2. Deve classificar como Conservador")
    void calculateProfile_Conservador() {
        Long clienteId = 2L;

        InvestmentHistory h1 = history("CDB Renda Fixa", 1000.0, -0.01);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Conservador", resp.perfil);
        assertEquals(13, resp.pontuacao); // 5 (retorno) + 5 (risco baixo) + 3 (pouca experiência)
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
        assertEquals(57, resp.pontuacao);
    }

    @Test
    @DisplayName("4. Agressivo com alta experiência (pontuação máxima)")
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
        assertEquals(100, resp.pontuacao);
    }

    @Test
    @DisplayName("5. Agressivo com penalidade (pouca experiência)")
    void calculateProfile_Agressivo_ComPenalidade() {
        Long clienteId = 5L;

        InvestmentHistory h1 = history("Ação", 1000.0, 0.20);
        InvestmentHistory h2 = history("FII", 500.0, 0.25);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1, h2));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Agressivo", resp.perfil);
        assertEquals(82, resp.pontuacao); // 92 - 10 de penalidade
    }

    // ========== AUMENTO DE COBERTURA ==========

    @Test
    @DisplayName("6. Tipos desconhecidos devem ser tratados como risco médio (score final Moderado)")
    void calculateProfile_TipoDesconhecido() {
        Long clienteId = 6L;

        InvestmentHistory h1 = history("CRYPTO RANDOM", 1000.0, 0.10);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertNotNull(resp);
        assertEquals("Moderado", resp.perfil);
    }

    @Test
    @DisplayName("7. Rentabilidade zero deve ser moderado para retorno, mas perfil final Conservador")
    void calculateProfile_RentabilidadeZero() {
        Long clienteId = 7L;

        InvestmentHistory h1 = history("Fundo Multimercado", 500.0, 0.0);

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Conservador", resp.perfil);
    }

    @Test
    @DisplayName("8. Rentabilidade extremamente alta (stress test)")
    void calculateProfile_RentabilidadeExtrema() {
        Long clienteId = 8L;

        InvestmentHistory h1 = history("Ação", 1000.0, 1.5); // 150%

        when(historyRepository.list("clienteId", clienteId))
                .thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Agressivo", resp.perfil);
        assertTrue(resp.pontuacao >= 60);
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
        assertTrue(resp.perfil.equals("Moderado") || resp.perfil.equals("Agressivo"));
    }

    @Test
    @DisplayName("11. Deve lidar com rentabilidade negativa extrema")
    void calculateProfile_RentabilidadeMuitoNegativa() {
        Long clienteId = 11L;

        InvestmentHistory h1 = history("CDB", 1000, -0.50);

        when(historyRepository.list("clienteId", clienteId)).thenReturn(List.of(h1));

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Conservador", resp.perfil);
    }

    @Test
    @DisplayName("12. Deve considerar corretamente quantidade MUITO alta de operações (stress test)")
    void calculateProfile_GrandeQuantidade() {
        Long clienteId = 12L;

        List<InvestmentHistory> historico = java.util.stream.IntStream.range(0, 200)
                .mapToObj(i -> history("Ação", 100 + i, 0.20))
                .toList();

        when(historyRepository.list("clienteId", clienteId)).thenReturn(historico);

        RiskProfileResponseDTO resp = riskProfileService.calculateProfile(clienteId);

        assertEquals("Agressivo", resp.perfil);
        assertEquals(100, resp.pontuacao); // pontuação máxima
    }
}
