package cef.financial.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    public String generateToken(String username, Set<String> roles) {

        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofHours(2));

        return Jwt
                .issuer("investment-api")
                .subject(username)
                .groups(roles)
                .issuedAt(now)
                .expiresAt(exp)
                .sign();
    }
}
