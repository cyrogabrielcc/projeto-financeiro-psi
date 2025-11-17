package cef.invest.Integrados;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class GenericExceptionMapperIT {

    @Test
    void genericExceptionDeveRetornar500ComApiErrorResponse() {
        given()
                .when()
                .get("/it-exceptions/generic")
                .then()
                .statusCode(500)
                .body("status", equalTo(500))
                .body("error", equalTo("Internal Server Error"))
                .body("message", equalTo("Ocorreu um erro inesperado ao processar sua requisição."))
                // path deve refletir o endpoint ou ser null, dependendo do UriInfo
                .body("timestamp", notNullValue());
    }
}