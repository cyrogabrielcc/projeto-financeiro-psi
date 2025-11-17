package cef.financial.domain.service;

import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.repository.InvestmentProductRepository; // Importe
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject; // Importe
import java.util.List;

@ApplicationScoped
public class RecommendationService {

    @Inject
    InvestmentProductRepository productRepository;

    public List<InvestmentProduct> recommendByProfile(String perfil) {
        if (perfil == null) {
            return List.of();
        }
        String normalizado = perfil.trim().toUpperCase();

        return productRepository.list("UPPER(perfilRecomendado) = ?1", normalizado);
    }
}