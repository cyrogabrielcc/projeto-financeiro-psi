package cef.invest.Exception;
import cef.financial.domain.exception.ErrorResponse;
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
            String path = resolvePathSafely();

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

    /**
     * Tenta obter o path da requisição atual.
     * Em testes (sem request ativa), evita IllegalStateException e retorna null.
     */
    private String resolvePathSafely() {
        if (uriInfo == null) {
            return null;
        }
        try {
            return uriInfo.getPath();
        } catch (IllegalStateException e) {
            // Sem request em andamento (ex: testes unitários)
            return null;
        }
    }
}