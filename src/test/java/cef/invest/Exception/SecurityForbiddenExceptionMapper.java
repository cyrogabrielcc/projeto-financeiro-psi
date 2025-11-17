package cef.invest.Exception;

import cef.financial.domain.exception.ErrorResponse;
import io.quarkus.security.ForbiddenException;
import jakarta.ws.rs.core.Context;
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

        String path = resolvePathSafely();

        ErrorResponse body = new ErrorResponse(
                Response.Status.FORBIDDEN.getStatusCode(),
                "Forbidden",
                "Acesso negado. O token não possui as permissões necessárias para este recurso.",
                path
        );

        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(body)
                .build();
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