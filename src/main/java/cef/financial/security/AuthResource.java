package cef.financial.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.*;

import java.util.Set;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    TokenService tokenService;

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {

        // Autenticação simplificada
        if ("admin".equals(request.username) && "admin123".equals(request.password)) {
            String token = tokenService.generateToken(
                    request.username,
                    Set.of("admin", "user")
            );

            NewCookie cookie = createJwtCookie(token);

            // Corpo sem token, só mensagem genérica
            return Response.ok()
                    .entity("{\"message\":\"login efetuado com sucesso\"}")
                    .cookie(cookie)
                    .build();

        } else if ("user".equals(request.username) && "user123".equals(request.password)) {
            String token = tokenService.generateToken(
                    request.username,
                    Set.of("user")
            );

            NewCookie cookie = createJwtCookie(token);

            return Response.ok()
                    .entity("{\"message\":\"login efetuado com sucesso\"}")
                    .cookie(cookie)
                    .build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"credenciais inválidas\"}")
                    .build();
        }
    }

    private NewCookie createJwtCookie(String token) {
        return new NewCookie(
                "access_token",      // nome do cookie
                token               // valor
                // path
                // domain
                // comment
                // secure (true em prod com https)
                // httpOnly
        );
    }
}
