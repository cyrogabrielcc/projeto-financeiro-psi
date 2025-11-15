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
        Instant exp = now.plus(Duration.ofHours(2)); // token válido por 2 horas

        return Jwt
                .issuer("investment-api")     // TEM que bater com mp.jwt.verify.issuer
                .subject(username)            // sub
                .groups(roles)                // claim 'groups' -> usado por @RolesAllowed
                .issuedAt(now)
                .expiresAt(exp)
                .sign();                      // assina usando smallrye.jwt.sign.key-location
    }
}
