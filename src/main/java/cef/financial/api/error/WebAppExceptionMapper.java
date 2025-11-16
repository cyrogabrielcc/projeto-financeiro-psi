package cef.financial.api.error;

import cef.financial.domain.exception.ApiErrorResponse;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;

@Provider
public class WebAppExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = Logger.getLogger(WebAppExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(WebApplicationException exception) {

        int status = exception.getResponse() != null
                ? exception.getResponse().getStatus()
                : Response.Status.BAD_REQUEST.getStatusCode();

        Throwable cause = exception.getCause();
        String friendlyMessage = null;

        // Verifica se a causa é erro de JSON (Jackson)
        while (cause != null) {
            if (cause instanceof JsonParseException) {
                friendlyMessage =
                        "Corpo da requisição inválido. Verifique se o JSON está bem formatado. " +
                                "Exemplo: campos de texto devem estar entre aspas, como \"tipoProduto\": \"FUNDO\".";
                break;
            }
            if (cause instanceof JsonMappingException) {
                friendlyMessage =
                        "Não foi possível interpretar os dados enviados. " +
                                "Verifique os tipos dos campos e o formato do JSON.";
                break;
            }
            cause = cause.getCause();
        }

        if (friendlyMessage == null) {
            // Para outros WebApplicationException (ex.: os que você lança no service)
            friendlyMessage = exception.getMessage() != null
                    ? exception.getMessage()
                    : "Erro ao processar a requisição.";
        }

        // Loga no servidor para depuração
        LOG.warn("Erro de requisição: " + friendlyMessage, exception);

        ApiErrorResponse error = new ApiErrorResponse();
        error.status = status;
        error.error = Response.Status.fromStatusCode(status) != null
                ? Response.Status.fromStatusCode(status).getReasonPhrase()
                : "Bad Request";
        error.message = friendlyMessage;
        error.path = uriInfo != null ? uriInfo.getPath() : null;
        error.timestamp = OffsetDateTime.now();

        return Response.status(status)
                .entity(error)
                .build();
    }
}
