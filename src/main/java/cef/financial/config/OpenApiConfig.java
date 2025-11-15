package cef.financial.config;

import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;

/**
 * Define o esquema de segurança Bearer JWT para o Swagger.
 * NÃO aplica segurança em nenhum endpoint ainda, só registra o esquema.
 */
@SecurityScheme(
        securitySchemeName = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
