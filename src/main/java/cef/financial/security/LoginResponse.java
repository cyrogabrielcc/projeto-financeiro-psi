package cef.financial.security;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Resposta de autenticação contendo o token JWT")
public class LoginResponse {

    @Schema(example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", description = "Token JWT")
    public String token;

    @Schema(example = "Bearer", description = "Tipo de autenticação")
    public String tipo = "Bearer";

    public LoginResponse() {
    }

    public LoginResponse(String token) {
        this.token = token;
    }
}
