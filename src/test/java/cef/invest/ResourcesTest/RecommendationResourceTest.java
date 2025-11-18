package cef.invest.ResourcesTest;

import cef.financial.api.resources.RecommendationResource;
import cef.financial.domain.dto.InvestmentProductResponseDTO;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.service.RecommendationService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.NotFoundException;
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
 * Testes unitários para RecommendationResource
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
        assertEquals(MediaType.APPLICATION_JSON,
                RecommendationResource.class.getAnnotation(Produces.class).value()[0]);

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

    // ===================== COMPORTAMENTO QUANDO LISTA VAZIA => NotFoundException =====================

    @Test
    void produtosRecomendados_deveLancar404_QuandoServicoRetornaVazio() {
        String perfil = "CONSERVADOR";
        when(recommendationService.recommendByProfile(perfil)).thenReturn(Collections.emptyList());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> recommendationResource.produtosRecomendados(perfil)
        );

        assertTrue(ex.getMessage().contains("CONSERSADOR") || ex.getMessage().contains("CONSERVADOR"));
        verify(recommendationService).recommendByProfile(perfil);
    }

    @Test
    void produtosRecomendados_deveLancar404_QuandoPerfilForNulo() {
        when(recommendationService.recommendByProfile(null)).thenReturn(Collections.emptyList());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> recommendationResource.produtosRecomendados(null)
        );

        assertTrue(ex.getMessage().contains("null"));
        verify(recommendationService).recommendByProfile(null);
    }

    @Test
    void produtosRecomendados_deveLancar404_QuandoPerfilForVazio() {
        when(recommendationService.recommendByProfile("")).thenReturn(Collections.emptyList());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> recommendationResource.produtosRecomendados("")
        );

        assertTrue(ex.getMessage().contains("perfil"));
        verify(recommendationService).recommendByProfile("");
    }

    @Test
    void produtosRecomendados_deveChamarServicoCorretamente_quandoNaoHaProdutos() {
        String perfil = "MODERADO";
        when(recommendationService.recommendByProfile(perfil)).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class,
                () -> recommendationResource.produtosRecomendados(perfil));

        verify(recommendationService, times(1)).recommendByProfile(perfil);
        verifyNoMoreInteractions(recommendationService);
    }

    // ===================== COMPORTAMENTO QUANDO EXISTEM PRODUTOS =====================

    @Test
    void produtosRecomendados_deveRetornarProdutosQuandoServicoRetornaDados() {
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

        List<InvestmentProductResponseDTO> result = recommendationResource.produtosRecomendados(perfil);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(recommendationService).recommendByProfile(perfil);
    }

    @Test
    void produtosRecomendados_deveConverterProdutosParaDTO() {
        String perfil = "CONSERVADOR";

        InvestmentProduct produto = new InvestmentProduct();
        produto.id = 1L;
        produto.nome = "Tesouro Direto";
        produto.tipo = "RENDA_FIXA";
        produto.rentabilidadeAnual = 0.08;
        produto.risco = "Baixa";

        when(recommendationService.recommendByProfile(perfil))
                .thenReturn(Arrays.asList(produto));

        List<InvestmentProductResponseDTO> result = recommendationResource.produtosRecomendados(perfil);

        assertNotNull(result);
        assertEquals(1, result.size());

        InvestmentProductResponseDTO dto = result.get(0);
        assertNotNull(dto);
        // se quiser, valide campos específicos aqui
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
        assertNotNull(result.get(0));
        assertNotNull(result.get(1));
        assertNotNull(result.get(2));

        verify(recommendationService).recommendByProfile(perfil);
    }


    @Test
    void testInstanciacaoResource() {
        RecommendationResource resource = new RecommendationResource();
        assertNotNull(resource);
    }

    @Test
    void produtosRecomendados_deveLancar404_ParaPerfisSemProdutos() {
        String[] perfis = {"CONSERVADOR", "MODERADO", "ARROJADO"};

        for (String perfil : perfis) {
            when(recommendationService.recommendByProfile(perfil))
                    .thenReturn(Collections.emptyList());

            assertThrows(NotFoundException.class,
                    () -> recommendationResource.produtosRecomendados(perfil));
        }

        for (String perfil : perfis) {
            verify(recommendationService).recommendByProfile(perfil);
        }
    }
}
