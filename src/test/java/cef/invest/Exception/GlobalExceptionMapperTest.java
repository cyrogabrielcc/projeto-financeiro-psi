package cef.invest.Exception;

import cef.financial.domain.exception.GlobalExceptionMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionMapperTest {

    private GlobalExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        // Instancia o mapper antes de cada teste
        mapper = new GlobalExceptionMapper();
    }


    @Test
    void deveRetornarRespostaOriginal_quandoExcecaoForWebApplicationException() {
        // Arrange (Preparação)

        // 1. Cria uma resposta customizada (ex: 404 Not Found)
        Response notFoundResponse = Response.status(Response.Status.NOT_FOUND)
                .entity("Recurso não encontrado")
                .type(MediaType.TEXT_PLAIN) // Usando TEXT_PLAIN de propósito para diferenciar
                .build();

        // 2. Cria a WebApplicationException encapsulando a resposta
        WebApplicationException wae = new WebApplicationException(
                "Exceção de teste para 404",
                notFoundResponse
        );

        // Act (Ação)
        Response resultResponse = mapper.toResponse(wae);

        // Assert (Verificação)

        // Verifica se a resposta retornada é A MESMA instância da resposta original
        assertSame(notFoundResponse, resultResponse,
                "A resposta deveria ser a mesma instância da exceção original.");

        // Validações adicionais para garantir
        assertEquals(404, resultResponse.getStatus());
        assertEquals("Recurso não encontrado", resultResponse.getEntity());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, resultResponse.getMediaType());
    }


    @Test
    void deveRetornarStatus500EmJson_quandoExcecaoForGenerica() {
        // Arrange (Preparação)

        // 1. Cria uma exceção genérica
        Throwable genericException = new NullPointerException("Erro inesperado de teste");

        // Act (Ação)
        Response resultResponse = mapper.toResponse(genericException);

        // Assert (Verificação)

        // 1. Verifica o status e o tipo da mídia
        assertEquals(500, resultResponse.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, resultResponse.getMediaType(),
                "O tipo de mídia deve ser application/json.");

        // 2. Verifica o corpo (entity) da resposta
        Object entity = resultResponse.getEntity();
        assertNotNull(entity, "O corpo da resposta não pode ser nulo.");

        // 3. Verifica se o corpo é do tipo esperado
        assertInstanceOf(GlobalExceptionMapper.SimpleErrorResponse.class, entity,
                "O corpo da resposta deve ser do tipo SimpleErrorResponse.");

        // 4. Valida o conteúdo do corpo
        GlobalExceptionMapper.SimpleErrorResponse errorBody = (GlobalExceptionMapper.SimpleErrorResponse) entity;
        assertEquals(500, errorBody.status);
        assertEquals("Erro interno no servidor.", errorBody.mensagem);
    }
}