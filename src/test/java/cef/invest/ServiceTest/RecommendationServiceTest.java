package cef.invest.ServiceTest;

import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.repository.InvestmentProductRepository; // Importe
import cef.financial.domain.service.RecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks; // Importe
import org.mockito.Mock; // Importe
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    InvestmentProductRepository productRepository;

    @InjectMocks
    RecommendationService recommendationService;

    // Não precisamos do setUp() para este teste simples

    @Test
    @DisplayName("1. Deve retornar uma lista vazia se o perfil for nulo")
    void recommendByProfile_Fail_PerfilNulo() {
        // Act
        List<InvestmentProduct> result = recommendationService.recommendByProfile(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("2. Deve retornar produtos para um perfil válido (normalizando a string)")
    void recommendByProfile_Success_PerfilValidoComNormalizacao() {
        // Arrange
        InvestmentProduct prodArrojado = new InvestmentProduct();
        prodArrojado.id = 1L;
        prodArrojado.nome = "Produto Arrojado";
        prodArrojado.perfilRecomendado = "ARROJADO";
        List<InvestmentProduct> mockList = List.of(prodArrojado);

        String perfilInput = " arrojado ";
        String perfilNormalizado = "ARROJADO";

        // 3. Mude o mock para o repositório
        when(productRepository.list("UPPER(perfilRecomendado) = ?1", perfilNormalizado))
                .thenReturn(mockList);

        // Act
        List<InvestmentProduct> result = recommendationService.recommendByProfile(perfilInput);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Produto Arrojado", result.get(0).nome);
    }

    @Test
    @DisplayName("3. Deve retornar lista vazia se nenhum produto corresponder ao perfil")
    void recommendByProfile_Success_NenhumProdutoEncontrado() {
        // Arrange
        String perfilInput = "INEXISTENTE";
        List<InvestmentProduct> emptyList = List.of();

        when(productRepository.list("UPPER(perfilRecomendado) = ?1", perfilInput))
                .thenReturn(emptyList);

        // Act
        List<InvestmentProduct> result = recommendationService.recommendByProfile(perfilInput);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}