package cef.financial.security;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Dados de login do usuário")
public class LoginRequest {

    @Schema(example = "user", description = "Nome de usuário")
    public String username;

    @Schema(example = "user123", description = "Senha do usuário")
    public String password;
}