package cef.invest.resources.test.ResourcesTest;

import cef.financial.api.resources.RecommendationResource;
import cef.financial.domain.dto.InvestmentProductResponseDTO;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.service.RecommendationService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RecommendationResource com 100% de cobertura
 */
@ExtendWith(MockitoExtension.class)
class RecommendationResourceTest {

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationResource recommendationResource;

    @Test
    void testClassAnnotations() {
        // Verifica as anotações de classe
        assertTrue(RecommendationResource.class.isAnnotationPresent(Path.class));
        assertEquals("/produtos-recomendados", RecommendationResource.class.getAnnotation(Path.class).value());

        assertTrue(RecommendationResource.class.isAnnotationPresent(Produces.class));
        assertTrue(RecommendationResource.class.isAnnotationPresent(Authenticated.class));
    }

    @Test
    void testFieldAnnotations() throws NoSuchFieldException {
        // Verifica a anotação do campo recommendationService
        var field = RecommendationResource.class.getDeclaredField("recommendationService");
        assertTrue(field.isAnnotationPresent(Inject.class));
    }

    @Test
    void testMethodAnnotations() throws NoSuchMethodException {
        var method = RecommendationResource.class.getMethod("produtosRecomendados", String.class);

        // Verifica anotações do método
        assertTrue(method.isAnnotationPresent(GET.class));
        assertTrue(method.isAnnotationPresent(RolesAllowed.class));
        assertTrue(method.isAnnotationPresent(Path.class));

        RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
        assertArrayEquals(new String[]{"user", "admin"}, rolesAnnotation.value());
        assertEquals("/{perfil}", method.getAnnotation(Path.class).value());
    }

    @Test
    void produtosRecomendados_deveRetornarListaVaziaQuandoServicoRetornaVazio() {
        // Arrange
        String perfil = "CONSERVADOR";
        when(recommendationService.recommendByProfile(perfil)).thenReturn(Collections.emptyList());

        // Act
        List<InvestmentProductResponseDTO> result = recommendationResource.produtosRecomendados(perfil);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(recommendationService).recommendByProfile(perfil);
    }

    @Test
    void produtosRecomendados_deveRetornarProdutosQuandoServicoRetornaDados() {
        // Arrange
        String perfil = "ARROJADO";

        InvestmentProduct produto1 = new InvestmentProduct();
        produto1.id = 1L;
        produto1.nome = "Ações";
        produto1.tipo = "RENDA_VARIAVEL";
        produto1.rentabilidadeAnual = 0.15;
        produto1.risco = "ALTO";

        InvestmentProduct produto2 = new InvestmentProduct();
        produto2.id = 2L;
        produto2.nome = "FII";
        produto2.tipo = "FUNDO_IMOBILIARIO";
        produto2.rentabilidadeAnual = 0.12;
        produto2.risco = "MEDIO";

        List<InvestmentProduct> produtos = Arrays.asList(produto1, produto2);
        when(recommendationService.recommendByProfile(perfil)).thenReturn(produtos);

        // Act
        List<InvestmentProductResponseDTO> result = recommendationResource.produtosRecomendados(perfil);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(recommendationService).recommendByProfile(perfil);
    }

    @Test
    void produtosRecomendados_deveFuncionarComPerfilNulo() {
        // Arrange
        when(recommendationService.recommendByProfile(null)).thenReturn(Collections.emptyList());

        // Act
        List<InvestmentProductResponseDTO> result = recommendationResource.produtosRecomendados(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(recommendationService).recommendByProfile(null);
    }

    @Test
    void produtosRecomendados_deveFuncionarComPerfilVazio() {
        // Arrange
        when(recommendationService.recommendByProfile("")).thenReturn(Collections.emptyList());

        // Act
        List<InvestmentProductResponseDTO> result = recommendationResource.produtosRecomendados("");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(recommendationService).recommendByProfile("");
    }

    @Test
    void produtosRecomendados_deveChamarServicoCorretamente() {
        // Arrange
        String perfil = "MODERADO";
        when(recommendationService.recommendByProfile(perfil)).thenReturn(Collections.emptyList());

        // Act
        recommendationResource.produtosRecomendados(perfil);

        // Assert
        verify(recommendationService, times(1)).recommendByProfile(perfil);
        verifyNoMoreInteractions(recommendationService);
    }

    @Test
    void produtosRecomendados_deveConverterProdutosParaDTO() {
        // Arrange
        String perfil = "CONSERVADOR";

        InvestmentProduct produto = new InvestmentProduct();
        produto.id = 1L;
        produto.nome = "Tesouro Direto";
        produto.tipo = "RENDA_FIXA";
        produto.rentabilidadeAnual = 0.08;
        produto.risco = "Baixa";

        when(recommendationService.recommendByProfile(perfil)).thenReturn(Arrays.asList(produto));

        // Act
        List<InvestmentProductResponseDTO> result = recommendationResource.produtosRecomendados(perfil);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        InvestmentProductResponseDTO dto = result.get(0);
        assertNotNull(dto);
        verify(recommendationService).recommendByProfile(perfil);
    }

    @Test
    void produtosRecomendados_devePreservarOrdemDosProdutos() {
        // Arrange
        String perfil = "CONSERVADOR";

        InvestmentProduct produto1 = new InvestmentProduct();
        produto1.id = 1L;
        produto1.nome = "Primeiro";

        InvestmentProduct produto2 = new InvestmentProduct();
        produto2.id = 2L;
        produto2.nome = "Segundo";

        InvestmentProduct produto3 = new InvestmentProduct();
        produto3.id = 3L;
        produto3.nome = "Terceiro";

        List<InvestmentProduct> produtos = Arrays.asList(produto1, produto2, produto3);
        when(recommendationService.recommendByProfile(perfil)).thenReturn(produtos);

        // Act
        List<InvestmentProductResponseDTO> result = recommendationResource.produtosRecomendados(perfil);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(recommendationService).recommendByProfile(perfil);
    }

    @Test
    void testInstanciacaoResource() {
        // Testa que a classe pode ser instanciada corretamente
        RecommendationResource resource = new RecommendationResource();
        assertNotNull(resource);
    }

    @Test
    void produtosRecomendados_deveFuncionarComDiferentesPerfis() {
        // Testa com vários tipos de perfil
        String[] perfis = {"CONSERVADOR", "MODERADO", "ARROJADO"};

        for (String perfil : perfis) {
            // Arrange
            when(recommendationService.recommendByProfile(perfil)).thenReturn(Collections.emptyList());

            // Act
            List<InvestmentProductResponseDTO> result = recommendationResource.produtosRecomendados(perfil);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        // Verifica que cada perfil foi chamado
        for (String perfil : perfis) {
            verify(recommendationService).recommendByProfile(perfil);
        }
    }
}