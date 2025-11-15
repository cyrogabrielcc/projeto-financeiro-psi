package cef.financial.domain.service;

import cef.financial.domain.dto.RiskProfileResponse;
import cef.financial.domain.model.InvestmentHistory;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RiskProfileService {

    public RiskProfileResponse calculateProfile(Long clienteId) {
        List<InvestmentHistory> history =
                InvestmentHistory.list("clienteId", clienteId);

        int score = 50;
        double totalValor = 0.0;
        int qtdOperacoes = history.size();

        for (InvestmentHistory h : history) {
            totalValor += h.valor;

            // proxy simples de risco: maior rentabilidade => maior risco
            if (h.rentabilidade >= 0.15) {
                score += 10;
            } else if (h.rentabilidade >= 0.10) {
                score += 5;
            } else {
                score -= 3;
            }

            // supondo que "Fundo Multimercado" é mais arriscado
            if (h.tipo != null && h.tipo.toLowerCase().contains("multimercado")) {
                score += 5;
            }
        }

        // Movimentação financeira: mais operações -> mais perfil ativo
        if (qtdOperacoes == 0) {
            score -= 10;
        } else if (qtdOperacoes > 5) {
            score += 5;
        }

        String perfil;
        String descricao;

        if (score <= 40) {
            perfil = "Conservador";
            descricao = "Perfil com baixa tolerância a risco, foco em segurança e liquidez.";
        } else if (score <= 70) {
            perfil = "Moderado";
            descricao = "Perfil equilibrado entre segurança e rentabilidade.";
        } else {
            perfil = "Agressivo";
            descricao = "Perfil com alta tolerância ao risco e foco em rentabilidade.";
        }

        RiskProfileResponse response = new RiskProfileResponse();
        response.clienteId = clienteId;
        response.perfil = perfil;
        response.pontuacao = score;
        response.descricao = descricao;
        return response;
    }
}
