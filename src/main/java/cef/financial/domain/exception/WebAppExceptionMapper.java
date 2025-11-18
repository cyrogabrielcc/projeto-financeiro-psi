package cef.financial.domain.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;

import static jakarta.ws.rs.core.Response.Status;

@Provider
public class WebAppExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = Logger.getLogger(WebAppExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(WebApplicationException exception) {

        int statusCode = exception.getResponse() != null
                ? exception.getResponse().getStatus()
                : Status.INTERNAL_SERVER_ERROR.getStatusCode();

        Status httpStatus = Status.fromStatusCode(statusCode);
        if (httpStatus == null) {
            httpStatus = Status.INTERNAL_SERVER_ERROR;
            statusCode = httpStatus.getStatusCode();
        }

        // Mensagem "bonitinha" para 401 e 403, como o teste espera
        String message;
        if (statusCode == Status.FORBIDDEN.getStatusCode()) {
            message = "Acesso negado. O token não possui as permissões necessárias para este recurso.";
        } else if (statusCode == Status.UNAUTHORIZED.getStatusCode()) {
            message = "Token ausente ou inválido. Envie um JWT Bearer válido no cabeçalho Authorization.";
        } else {
            // fallback pra outros casos de WebApplicationException
            message = exception.getMessage() != null
                    ? exception.getMessage()
                    : httpStatus.getReasonPhrase();
        }

        ApiErrorResponse body = new ApiErrorResponse();
        body.setStatus(statusCode);
        body.setError(httpStatus.getReasonPhrase());              // "Unauthorized" ou "Forbidden"
        body.setMessage(message);
        body.setPath(uriInfo != null ? uriInfo.getPath() : null);
        body.setTimestamp(OffsetDateTime.now());

        LOG.warnf("Erro de negócio (WebApplicationException): HTTP %d %s",
                statusCode, httpStatus.getReasonPhrase());

        // IMPORTANTE: sempre construir uma nova Response com entity
        return Response
                .status(statusCode)
                .entity(body)
                .build();
    }
}
