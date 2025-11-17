package cef.financial.ErrorMessage.AuthErrorMessage;

import cef.financial.ErrorMessage.ErrorResponse;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SecurityUnauthorizedExceptionMapper implements ExceptionMapper<Throwable> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {

        if (exception instanceof NotAuthorizedException || exception instanceof UnauthorizedException) {
            String path = uriInfo != null ? uriInfo.getPath() : null;

            ErrorResponse body = new ErrorResponse(
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    "Unauthorized",
                    "Token ausente ou inválido. Envie um JWT Bearer válido no cabeçalho Authorization.",
                    path
            );

            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(body)
                    .build();
        }

        // Se não for erro de segurança, deixa outro mapper tratar
        return Response.serverError().build();
    }
}
