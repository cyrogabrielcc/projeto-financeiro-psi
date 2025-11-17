package cef.financial.domain.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class WebAppExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = Logger.getLogger(WebAppExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException exception) {
        // Loga no nível WARN só pra ter rastro
        LOG.warnf(exception, "Erro de negócio (WebApplicationException): %s", exception.getMessage());

        // **PONTO IMPORTANTE**: devolve exatamente o Response carregado na exceção
        Response response = exception.getResponse();

        // Se por algum motivo vier null (raro, mas por segurança):
        if (response == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(exception.getMessage())
                    .build();
        }

        return response;
    }
}
