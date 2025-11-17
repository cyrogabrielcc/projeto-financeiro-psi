package cef.financial.security;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Set;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(
        name = "Autenticação",                      // 🔥 força esse grupo a ser o primeiro
        description = "Endpoint de login e geração de token JWT"
)
@SecurityRequirement(name = "none")                // explícito para remover exigência de JWT
public class AuthResource {

    @Inject
    TokenService tokenService;

    public AuthResource() {}

    public AuthResource(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @POST
    @Path("/login")
    @PermitAll
    @Operation(
            summary = "Autenticar usuário e gerar token JWT",
            description = "Recebe credenciais de login e retorna um token JWT válido para acessar os demais endpoints."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requisição inválida: campos obrigatórios ausentes"
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas"
            )
    })
    public Response login(
            @RequestBody(
                    required = true,
                    description = "Credenciais do usuário para autenticação",
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class)
                    )
            )
            LoginRequest request
    ) {
        if (request == null || request.username == null || request.password == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // ADMIN login
        if ("admin".equals(request.username) && "admin123".equals(request.password)) {
            String token = tokenService.generateToken(
                    request.username,
                    Set.of("admin", "user")
            );
            return Response.ok(new LoginResponse(token)).build();
        }

        // USER login
        if ("user".equals(request.username) && "user123".equals(request.password)) {
            String token = tokenService.generateToken(
                    request.username,
                    Set.of("user")
            );
            return Response.ok(new LoginResponse(token)).build();
        }


        // Falha de autenticação
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
