package cef.invest.Exception;

import cef.financial.domain.exception.ApiErrorResponse;
import cef.financial.domain.exception.GenericExceptionMapper;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenericExceptionMapperTest {

    @Mock
    UriInfo uriInfo;

    @InjectMocks
    GenericExceptionMapper mapper;

    @Test
    void deveRetornarStatus500ComBodyCorreto() {
        String testPath = "/api/v1/some-endpoint";
        Throwable exception = new NullPointerException("Erro de teste simulado");

        when(uriInfo.getPath()).thenReturn(testPath);

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(500, resultResponse.getStatus());
        Object entity = resultResponse.getEntity();
        assertNotNull(entity);
        assertInstanceOf(ApiErrorResponse.class, entity);

        ApiErrorResponse body = (ApiErrorResponse) entity;
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getError());
        assertEquals("Ocorreu um erro inesperado ao processar sua requisição.", body.getMessage());
        assertEquals(testPath, body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void deveRetornarStatus500ComCaminhoNulo_quandoUriInfoForNulo() {
        mapper.uriInfo = null;
        Throwable exception = new RuntimeException("Outro erro simulado");

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(500, resultResponse.getStatus());
        Object entity = resultResponse.getEntity();
        assertNotNull(entity);
        assertInstanceOf(ApiErrorResponse.class, entity);

        ApiErrorResponse body = (ApiErrorResponse) entity;
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getError());
        assertEquals("Ocorreu um erro inesperado ao processar sua requisição.", body.getMessage());
        assertNull(body.getPath());
        assertNotNull(body.getTimestamp());
    }
}