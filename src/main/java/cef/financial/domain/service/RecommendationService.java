package cef.financial.domain.service;

import cef.financial.domain.model.InvestmentProduct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RecommendationService {

    public List<InvestmentProduct> recommendByProfile(String perfil) {
        if (perfil == null) {
            return List.of();
        }
        String normalizado = perfil.trim().toUpperCase();
        return InvestmentProduct.list("UPPER(perfilRecomendado) = ?1", normalizado);
    }
}

