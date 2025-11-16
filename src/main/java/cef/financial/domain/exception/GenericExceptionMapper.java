package cef.financial.domain.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        // Log completo no servidor
        LOG.error("Erro inesperado na API", exception);

        ApiErrorResponse error = new ApiErrorResponse();
        error.status = 500;
        error.error = "Internal Server Error";
        error.message = "Ocorreu um erro inesperado ao processar sua requisição.";
        error.path = uriInfo != null ? uriInfo.getPath() : null;
        error.timestamp = OffsetDateTime.now();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .build();
    }
}
