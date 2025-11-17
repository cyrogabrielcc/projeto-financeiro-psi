package cef.invest.ResourcesTest;

import cef.financial.security.TokenService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    TokenService tokenService = new TokenService();

    @Test
    void generateToken_DeveGerarTokenValidoComClaimsCorretos() {
        // Arrange
        String username = "ana";
        Set<String> roles = Set.of("user", "admin");

        // Act
        String token = tokenService.generateToken(username, roles);

        // Asserts básicos
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(token.contains("."), "Token JWT deve ter pelo menos dois pontos");

        // JWT = header.payload.signature
        String[] partes = token.split("\\.");
        assertEquals(3, partes.length, "JWT deve ter 3 partes (header.payload.signature)");

        // Decodifica o payload (segunda parte)
        String payloadB64 = partes[1];
        byte[] decoded = Base64.getUrlDecoder().decode(payloadB64);
        String json = new String(decoded, StandardCharsets.UTF_8);

        // Verificações sobre o JSON de payload
        // issuer
        assertTrue(json.contains("\"iss\":\"investment-api\""),
                "Payload deve conter issuer 'investment-api'");

        // subject
        assertTrue(json.contains("\"sub\":\"" + username + "\""),
                "Payload deve conter subject com o username");

        // groups / roles (não testamos ordem, pq Set não garante ordem)
        assertTrue(json.contains("\"groups\""),
                "Payload deve conter a claim 'groups'");
        assertTrue(json.contains("user"),
                "Payload deve conter o grupo 'user'");
        assertTrue(json.contains("admin"),
                "Payload deve conter o grupo 'admin'");

        // timestamps
        assertTrue(json.contains("\"iat\""),
                "Payload deve conter a claim 'iat' (issued at)");
        assertTrue(json.contains("\"exp\""),
                "Payload deve conter a claim 'exp' (expiration)");
    }
}
