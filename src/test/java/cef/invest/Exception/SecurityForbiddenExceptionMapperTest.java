package cef.invest.Exception;

import cef.financial.domain.exception.ErrorResponse;
import cef.financial.domain.exception.SecurityForbiddenExceptionMapper;
import io.quarkus.security.ForbiddenException;
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
class SecurityForbiddenExceptionMapperTest {

    @Mock
    UriInfo uriInfo;

    @InjectMocks
    SecurityForbiddenExceptionMapper mapper;

    @Test
    void deveRetornarStatus403ComBodyCorreto() {
        String testPath = "/api/protected/resource";
        ForbiddenException exception = new ForbiddenException("Token inválido");

        when(uriInfo.getPath()).thenReturn(testPath);

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(403, resultResponse.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, resultResponse.getMediaType());

        Object entity = resultResponse.getEntity();
        assertNotNull(entity);
        assertInstanceOf(ErrorResponse.class, entity);

        ErrorResponse errorBody = (ErrorResponse) entity;
        assertNotNull(errorBody.getTimestamp());
        assertEquals(403, errorBody.getStatus());
        assertEquals("Forbidden", errorBody.getError());
        assertEquals("Acesso negado. O token não possui as permissões necessárias para este recurso.", errorBody.getMessage());
        assertEquals(testPath, errorBody.getPath());
    }

    @Test
    void deveRetornarStatus403ComCaminhoNulo_quandoUriInfoForNulo() {
        ForbiddenException exception = new ForbiddenException("Token inválido");

        mapper.uriInfo = null;

        Response resultResponse = mapper.toResponse(exception);

        assertEquals(403, resultResponse.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, resultResponse.getMediaType());

        Object entity = resultResponse.getEntity();
        assertNotNull(entity);
        assertInstanceOf(ErrorResponse.class, entity);

        ErrorResponse errorBody = (ErrorResponse) entity;
        assertNotNull(errorBody.getTimestamp());
        assertEquals(403, errorBody.getStatus());
        assertEquals("Forbidden", errorBody.getError());
        assertEquals("Acesso negado. O token não possui as permissões necessárias para este recurso.", errorBody.getMessage());
        assertNull(errorBody.getPath());
    }
}