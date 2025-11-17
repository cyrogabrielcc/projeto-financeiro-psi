package cef.invest.Integrados;

import io.quarkus.security.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Path;

@Path("/it-exceptions")
public class TestExceptionResource {

    @GET
    @Path("/forbidden")
    public void forbidden() {
        // Vai cair no SecurityForbiddenExceptionMapper
        throw new ForbiddenException("Sem permissão");
    }

    @GET
    @Path("/unauthorized")
    public void unauthorized() {
        // Vai cair no SecurityUnauthorizedExceptionMapper
        throw new NotAuthorizedException("Bearer");
    }

    @GET
    @Path("/generic")
    public void generic() {
        // Deve cair no GenericExceptionMapper
        throw new RuntimeException("Erro inesperado");
    }
}