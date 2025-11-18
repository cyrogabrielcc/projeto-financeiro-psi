package cef.financial.security;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirementsSet;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Set;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final TokenService tokenService;

    // construtor que o teste está usando
    public AuthResource(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @POST
    @Path("/login")
    @PermitAll
    @SecurityRequirementsSet   // <<< ESSA É UMA DAS QUE O TESTE COBRA
    @Tag(name = "1 - LOGIN DE USUÁRIO")
    @Operation(summary = "Realiza login e retorna um token JWT")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @APIResponse(responseCode = "400", description = "Requisição inválida"),
            @APIResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public Response login(@RequestBody(description = "Credenciais do usuário") LoginRequest request) {
        if (request == null || request.username == null || request.password == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if ("admin".equals(request.username) && "admin123".equals(request.password)) {
            var roles = Set.of("admin", "user");
            String token = tokenService.generateToken("admin", roles);
            return Response.ok(new LoginResponse(token)).build();
        }

        if ("user".equals(request.username) && "user123".equals(request.password)) {
            var roles = Set.of("user");
            String token = tokenService.generateToken("user", roles);
            return Response.ok(new LoginResponse(token)).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
