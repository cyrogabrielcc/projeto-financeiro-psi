package cef.invest.Exception;
import cef.financial.domain.exception.ApiErrorResponse;
import cef.financial.domain.exception.WebAppExceptionMapper;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebAppExceptionMapperTest {

    @Mock
    UriInfo uriInfo;

    @InjectMocks
    WebAppExceptionMapper mapper;

    private final String testPath = "/api/v1/test/resource";

    @BeforeEach
    void setUp() {
        when(uriInfo.getPath()).thenReturn(testPath);
    }

    @Test
    void deveRetornarStatus401ComMensagemCustomizada() {
        WebApplicationException exception = new NotAuthorizedException("Desafio de autenticação");

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(401, resultResponse.getStatus());
        Object entity = resultResponse.getEntity();
        assertInstanceOf(ApiErrorResponse.class, entity);

        ApiErrorResponse body = (ApiErrorResponse) entity;
        assertEquals(401, body.getStatus());
        assertEquals("Unauthorized", body.getError());
        assertEquals("Token ausente ou inválido. Envie um JWT Bearer válido no cabeçalho Authorization.", body.getMessage());
        assertEquals(testPath, body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void deveRetornarStatus403ComMensagemCustomizada() {
        WebApplicationException exception = new ForbiddenException("Acesso negado");

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(403, resultResponse.getStatus());
        Object entity = resultResponse.getEntity();
        assertInstanceOf(ApiErrorResponse.class, entity);

        ApiErrorResponse body = (ApiErrorResponse) entity;
        assertEquals(403, body.getStatus());
        assertEquals("Forbidden", body.getError());
        assertEquals("Acesso negado. O token não possui as permissões necessárias para este recurso.", body.getMessage());
        assertEquals(testPath, body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void deveRetornarStatus404ComMensagemDaExcecao() {
        String exceptionMessage = "Recurso não encontrado";
        WebApplicationException exception = new NotFoundException(exceptionMessage);

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(404, resultResponse.getStatus());
        Object entity = resultResponse.getEntity();
        assertInstanceOf(ApiErrorResponse.class, entity);

        ApiErrorResponse body = (ApiErrorResponse) entity;
        assertEquals(404, body.getStatus());
        assertEquals("Not Found", body.getError());
        assertEquals(exceptionMessage, body.getMessage());
        assertEquals(testPath, body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void deveRetornarStatus500_quandoStatusForDesconhecido() {
        String exceptionMessage = "Erro customizado";
        Response customErrorResponse = Response.status(599).build();
        WebApplicationException exception = new WebApplicationException(exceptionMessage, customErrorResponse);

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(500, resultResponse.getStatus());
        Object entity = resultResponse.getEntity();
        assertInstanceOf(ApiErrorResponse.class, entity);

        ApiErrorResponse body = (ApiErrorResponse) entity;
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getError());
        assertEquals(exceptionMessage, body.getMessage());
        assertEquals(testPath, body.getPath());
        assertNotNull(body.getTimestamp());
    }
}
