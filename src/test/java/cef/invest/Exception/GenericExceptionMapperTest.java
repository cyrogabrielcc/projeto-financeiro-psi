package cef.invest.Exception;
import cef.financial.domain.exception.GenericExceptionMapper;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenericExceptionMapperTest {

    @Test
    void testGenericExceptionMapperReturns500WithApiErrorResponseBody() {
        // arrange
        GenericExceptionMapper mapper = new GenericExceptionMapper();
        RuntimeException exception = new RuntimeException("Erro inesperado");

        // act
        Response response = mapper.toResponse(exception);

        // assert
        assertNotNull(response, "A Response não deveria ser nula");
        assertEquals(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                response.getStatus(),
                "O status HTTP esperado é 500"
        );

        Object entity = response.getEntity();
        assertNotNull(entity, "O corpo da resposta não deveria ser nulo");
    }
}
