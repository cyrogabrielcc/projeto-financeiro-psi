package cef.invest.Integrados;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/it-exceptions")
public class TestExceptionResource {

    @GET
    @Path("/generic")
    public void generic() {
        // NÃO retorna Response, precisa JOGAR exceção
        throw new RuntimeException("Erro genérico para testar GenericExceptionMapper");
    }

    @GET
    @Path("/forbidden")
    public void forbidden() {
        throw new jakarta.ws.rs.ForbiddenException("Acesso negado (simulação de 403)");
    }

    @GET
    @Path("/unauthorized")
    public void unauthorized() {
        throw new jakarta.ws.rs.NotAuthorizedException("Bearer");
    }
}
