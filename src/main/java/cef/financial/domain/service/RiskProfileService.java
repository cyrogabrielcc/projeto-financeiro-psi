package cef.financial.domain.service;

import cef.financial.domain.dto.RiskProfileResponseDTO;
import cef.financial.domain.model.InvestmentHistory;
import cef.financial.domain.repository.InvestmentHistoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class RiskProfileService {

    @Inject
    InvestmentHistoryRepository historyRepository;

    @Transactional
    public RiskProfileResponseDTO calculateProfile(Long clienteId) {

        List<InvestmentHistory> history =
                historyRepository.list("clienteId", clienteId);

        // Sempre 200, mas com perfil "Indefinido" quando não há histórico
        if (history == null || history.isEmpty()) {
            RiskProfileResponseDTO response = new RiskProfileResponseDTO();
            response.clienteId = clienteId;
            response.perfil = "Indefinido";
            response.pontuacao = 0;
            response.descricao = "Cliente sem histórico de investimentos suficiente para cálculo de perfil de risco.";
            return response;
        }

        int qtdOperacoes = history.size();
        double totalValor = 0.0;
        double weightedReturnSum = 0.0;
        int maxRiskLevel = 0; // 1 = baixo, 2 = médio, 3 = alto

        for (InvestmentHistory h : history) {
            double valor = h.valor;           // se for Double, trate null conforme necessário
            double rentabilidade = h.rentabilidade;

            totalValor += valor;

            // rentabilidade média ponderada pelo valor investido
            if (valor > 0) {
                weightedReturnSum += rentabilidade * valor;
            } else {
                weightedReturnSum += rentabilidade;
            }

            int riskLevel = deriveRiskLevel(h.tipo);
            if (riskLevel > maxRiskLevel) {
                maxRiskLevel = riskLevel;
            }
        }

        double avgReturn;
        if (totalValor > 0) {
            avgReturn = weightedReturnSum / totalValor;
        } else {
            avgReturn = weightedReturnSum / qtdOperacoes;
        }

        // ===== 1) Score pela rentabilidade média (0 a ~60) =====
        double returnScore;
        if (avgReturn <= 0) {
            returnScore = 5;          // rentabilidade ruim/negativa
        } else if (avgReturn <= 0.05) {
            returnScore = 20;         // até 5% ao ano
        } else if (avgReturn <= 0.10) {
            returnScore = 35;         // até 10% ao ano
        } else if (avgReturn <= 0.15) {
            returnScore = 50;         // até 15% ao ano
        } else {
            returnScore = 60;         // acima de 15% ao ano
        }

        // ===== 2) Score pela exposição ao risco (0 a 25) =====
        double riskExposureScore;
        switch (maxRiskLevel) {
            case 1: // renda fixa / baixo risco
                riskExposureScore = 5;
                break;
            case 2: // multimercado / médio risco
                riskExposureScore = 15;
                break;
            case 3: // ações / FIIs / renda variável
                riskExposureScore = 25;
                break;
            default:
                riskExposureScore = 0;
        }

        // ===== 3) Score pela experiência (qtd de operações) (0 a 15) =====
        double experienceScore;
        if (qtdOperacoes <= 1) {
            experienceScore = 3;
        } else if (qtdOperacoes <= 3) {
            experienceScore = 7;
        } else if (qtdOperacoes <= 10) {
            experienceScore = 12;
        } else {
            experienceScore = 15;
        }

        double rawScore = returnScore + riskExposureScore + experienceScore;

        if (qtdOperacoes < 3 && maxRiskLevel == 3) {
            rawScore -= 10;
        }

        // Normaliza entre 0 e 100
        int score = (int) Math.round(Math.max(0, Math.min(100, rawScore)));

        String perfil;
        String descricao;

        if (score <= 30) {
            perfil = "Conservador";
            descricao = "Perfil com baixa tolerância a risco, priorizando segurança e preservação do capital.";
        } else if (score <= 60) {
            perfil = "Moderado";
            descricao = "Perfil equilibrado, disposto a assumir algum risco em busca de melhor rentabilidade.";
        } else {
            perfil = "Agressivo";
            descricao = "Perfil com alta tolerância ao risco, aceitando maior volatilidade em troca de potenciais ganhos.";
        }

        RiskProfileResponseDTO response = new RiskProfileResponseDTO();
        response.clienteId = clienteId;
        response.perfil = perfil;
        response.pontuacao = score;
        response.descricao = descricao;
        return response;
    }

    private int deriveRiskLevel(String tipo) {
        if (tipo == null) {
            return 1; // assume baixo se não souber
        }

        String t = tipo.toLowerCase();

        // Alto risco: ações, renda variável, FIIs
        if (t.contains("ação") || t.contains("acoes")
                || t.contains("renda variável") || t.contains("renda variavel")
                || t.contains("fii")
                || t.contains("fundo imobiliário") || t.contains("fundo imobiliario")) {
            return 3;
        }

        // Médio risco: multimercado, alguns fundos
        if (t.contains("multimercado") || t.contains("fundo")) {
            return 2;
        }

        if (t.contains("cdb")
                || t.contains("lci")
                || t.contains("lca")
                || t.contains("lc")
                || t.contains("tesouro")
                || t.contains("renda fixa")) {
            return 1;
        }

        // Default: baixo
        return 1;
    }
}
