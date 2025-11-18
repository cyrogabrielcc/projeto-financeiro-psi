package cef.financial.domain.service;

import cef.financial.domain.dto.RiskProfileResponseDTO;
import cef.financial.domain.model.InvestmentHistory;
import cef.financial.domain.model.Customer;
import cef.financial.domain.repository.InvestmentHistoryRepository;
import cef.financial.domain.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class RiskProfileService {

    @Inject
    InvestmentHistoryRepository historyRepository;

    @Inject
    CustomerRepository customerRepository;

    @Transactional
    public RiskProfileResponseDTO calculateProfile(Long clienteId) {

        // ===== Validação simples do ID do cliente =====
        if (clienteId == null || clienteId <= 0) {
            RiskProfileResponseDTO response = new RiskProfileResponseDTO();
            response.clienteId = clienteId;
            response.perfil = "Inválido";
            response.pontuacao = 0;
            response.descricao = "ID do cliente inválido. O valor deve ser um número positivo.";
            return response;
        }

        List<InvestmentHistory> history =
                historyRepository.list("clienteId", clienteId);

        // Sem histórico → perfil indefinido (não altera o cliente)
        if (history == null || history.isEmpty()) {
            RiskProfileResponseDTO response = new RiskProfileResponseDTO();
            response.clienteId = clienteId;
            response.perfil = "Indefinido";
            response.pontuacao = 0;
            response.descricao = "Cliente sem histórico de investimentos suficiente para cálculo de perfil de risco.";
            return response;
        }

        int qtdOperacoes = history.size();
        double pesoTotal = 0.0;
        double weightedReturn = 0.0;
        int maxRiskLevel = 0; // 1 = baixo, 2 = médio, 3 = alto

        for (InvestmentHistory h : history) {
            double valor = h.valor;
            double rentabilidade = h.rentabilidade;

            double peso = valor > 0 ? valor : 1.0;
            pesoTotal += peso;
            weightedReturn += rentabilidade * peso;

            int riskLevel = deriveRiskLevel(h.tipo);
            if (riskLevel > maxRiskLevel) {
                maxRiskLevel = riskLevel;
            }
        }

        double avgReturn = (pesoTotal > 0) ? (weightedReturn / pesoTotal) : 0.0;

        // ===== 1) Score pela rentabilidade média (0 a ~60) =====
        double returnScore;
        if (avgReturn <= 0) {
            returnScore = 5;
        } else if (avgReturn <= 0.05) {
            returnScore = 20;
        } else if (avgReturn <= 0.10) {
            returnScore = 35;
        } else if (avgReturn <= 0.15) {
            returnScore = 50;
        } else {
            returnScore = 60;
        }

        // ===== 2) Score pela exposição ao risco (0 a 25) =====
        double riskExposureScore;
        switch (maxRiskLevel) {
            case 1 -> riskExposureScore = 5;
            case 2 -> riskExposureScore = 15;
            case 3 -> riskExposureScore = 25;
            default -> riskExposureScore = 0;
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

        // Penalização para pouca experiência com risco alto
        if (qtdOperacoes < 3 && maxRiskLevel == 3) {
            rawScore -= 10;
        }

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

        // ===== 4) Atualiza o perfil do cliente na tabela CUSTOMER =====
        Customer customer = customerRepository.findById(clienteId);
        if (customer != null) {
            customer.perfil = perfil;
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
            return 1;
        }

        String t = tipo.toLowerCase();

        if (t.contains("ação") || t.contains("acoes")
                || t.contains("renda variável") || t.contains("renda variavel")
                || t.contains("fii")
                || t.contains("fundo imobiliário") || t.contains("fundo imobiliario")) {
            return 3;
        }

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

        return 1;
    }
}
