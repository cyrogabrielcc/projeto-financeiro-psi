package cef.financial.domain.service;

import cef.financial.domain.model.InvestmentProduct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RecommendationService {

    public List<InvestmentProduct> recommendByProfile(String perfil) {
        return InvestmentProduct.list("perfilRecomendado = ?1", perfil);
    }
}
