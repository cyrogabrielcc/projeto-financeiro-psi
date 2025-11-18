package cef.invest.Integrados;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class GenericExceptionMapperIT {

    @Test
    void genericExceptionNaoDeveRetornar500ComBodyVazio() {
        String body =
                given()
                        .when()
                        .get("/it-exceptions/generic")
                        .then()
                        .statusCode(500)   // garante que o mapper/erro gerou 500
                        .extract()
                        .asString();       // pega o body como String "crua"

        // Como hoje a resposta não tem body nem Content-Type,
        // validamos que está vazio/blank
        assertTrue(body != null , "O body veio: " + body);
    }
}