package cef.financial.domain.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        // Logar sempre o erro inesperado
        LOG.error("Erro inesperado na API.", exception);

        ApiErrorResponse body = new ApiErrorResponse();
        body.status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        body.message = "Ocorreu um erro interno ao processar a requisição.";
        body.error = exception.getClass().getSimpleName();
        body.timestamp = OffsetDateTime.now(ZoneOffset.UTC);

        // se quiser, path pode ser preenchido via RequestContext em outro mapper mais específico
        body.path = null;

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(body)
                .build();
    }

    // DTO simples para o corpo de erro
    public static class ApiErrorResponse {
        public int status;
        public String message;
        public String error;
        public OffsetDateTime timestamp;
        public String path;
    }
}
