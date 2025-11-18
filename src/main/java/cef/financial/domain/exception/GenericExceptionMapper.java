package cef.financial.domain.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.OffsetDateTime;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Context
    public UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {

        ApiErrorResponse error = new ApiErrorResponse();
        error.setStatus(INTERNAL_SERVER_ERROR.getStatusCode());       // 500
        error.setError(INTERNAL_SERVER_ERROR.getReasonPhrase());      // "Internal Server Error"
        error.setMessage("Ocorreu um erro inesperado ao processar sua requisição.");
        error.setPath(uriInfo != null ? uriInfo.getPath() : null);
        error.setTimestamp(OffsetDateTime.now());

        return Response.status(INTERNAL_SERVER_ERROR)
                .entity(error)
                .build();
    }
}