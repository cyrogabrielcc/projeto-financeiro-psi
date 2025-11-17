package cef.financial.domain.exception;

import io.quarkus.security.ForbiddenException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SecurityForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ForbiddenException exception) {

        String path = (uriInfo != null ? uriInfo.getPath() : null);

        ErrorResponse body = new ErrorResponse(
                Response.Status.FORBIDDEN.getStatusCode(),
                "Forbidden",
                "Acesso negado. O token não possui as permissões necessárias para este recurso.",
                path
        );

        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
