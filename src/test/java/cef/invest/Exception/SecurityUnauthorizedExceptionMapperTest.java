package cef.invest.Exception;

import cef.financial.domain.exception.ErrorResponse;
import cef.financial.domain.exception.SecurityUnauthorizedExceptionMapper;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.MediaType;
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
class SecurityUnauthorizedExceptionMapperTest {

    @Mock
    UriInfo uriInfo;

    @InjectMocks
    SecurityUnauthorizedExceptionMapper mapper;

    private final String expectedMessage = "Token ausente ou inválido. Envie um JWT Bearer válido no cabeçalho Authorization.";
    private final String expectedError = "Unauthorized";

    @Test
    void deveRetornarStatus401_quandoExcecaoForNotAuthorizedException() {
        String testPath = "/api/v1/secure-data";
        when(uriInfo.getPath()).thenReturn(testPath);

        NotAuthorizedException exception = new NotAuthorizedException("Desafio JAX-RS");

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), resultResponse.getStatus());

        Object entity = resultResponse.getEntity();
        assertNotNull(entity);
        assertInstanceOf(ErrorResponse.class, entity);

        ErrorResponse errorBody = (ErrorResponse) entity;
        assertNotNull(errorBody.getTimestamp());
        assertEquals(401, errorBody.getStatus());
        assertEquals(expectedError, errorBody.getError());
        assertEquals(expectedMessage, errorBody.getMessage());
        assertEquals(testPath, errorBody.getPath());
    }

    @Test
    void deveRetornarStatus401_quandoExcecaoForUnauthorizedException() {
        String testPath = "/api/v1/other-secure-data";
        when(uriInfo.getPath()).thenReturn(testPath);

        UnauthorizedException exception = new UnauthorizedException("Erro Quarkus Security");

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), resultResponse.getStatus());

        Object entity = resultResponse.getEntity();
        assertNotNull(entity);
        assertInstanceOf(ErrorResponse.class, entity);

        ErrorResponse errorBody = (ErrorResponse) entity;
        assertNotNull(errorBody.getTimestamp());
        assertEquals(401, errorBody.getStatus());
        assertEquals(expectedError, errorBody.getError());
        assertEquals(expectedMessage, errorBody.getMessage());
        assertEquals(testPath, errorBody.getPath());
    }

    @Test
    void deveRetornarStatus500_quandoExcecaoForOutroThrowable() {
        Throwable exception = new NullPointerException("Um erro qualquer");

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resultResponse.getStatus());
        assertNull(resultResponse.getEntity());
    }

    @Test
    void deveRetornarStatus401ComCaminhoNulo_quandoUriInfoForNulo() {
        mapper.uriInfo = null;
        NotAuthorizedException exception = new NotAuthorizedException("Desafio JAX-RS");

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), resultResponse.getStatus());

        Object entity = resultResponse.getEntity();
        assertNotNull(entity);
        assertInstanceOf(ErrorResponse.class, entity);

        ErrorResponse errorBody = (ErrorResponse) entity;
        assertNotNull(errorBody.getTimestamp());
        assertEquals(401, errorBody.getStatus());
        assertEquals(expectedError, errorBody.getError());
        assertEquals(expectedMessage, errorBody.getMessage());
        assertNull(errorBody.getPath());
    }
}