package cef.financial.security;

public class LoginResponse {
    public String token;
    public String tipo = "Bearer";

    public LoginResponse(String token) {
        this.token = token;
    }
}
