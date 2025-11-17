package cef.invest.Integrados;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class SecurityExceptionMappersIT {

    @Test
    void forbiddenDeveRetornar403ComCorpoPadrao() {
        given()
                .when()
                .get("/it-exceptions/forbidden")
                .then()
                .statusCode(403)
                .body("status", equalTo(403))
                .body("error", equalTo("Forbidden"))
                .body("message", equalTo("Acesso negado. O token não possui as permissões necessárias para este recurso."))
                // path pode ser null ou "/it-exceptions/forbidden" dependendo do UriInfo
                .body("timestamp", notNullValue());
    }

    @Test
    void unauthorizedDeveRetornar401ComCorpoPadrao() {
        given()
                .when()
                .get("/it-exceptions/unauthorized")
                .then()
                .statusCode(401)
                .body("status", equalTo(401))
                .body("error", equalTo("Unauthorized"))
                .body("message", equalTo("Token ausente ou inválido. Envie um JWT Bearer válido no cabeçalho Authorization."))
                .body("timestamp", notNullValue());
    }
}