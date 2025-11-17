package cef.financial.api.resources;

import cef.financial.domain.dto.InvestmentSimulationRequestDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class SimulationResourceIT {

    @Test
    void simularInvestimento_DeveRetornar200EBodyValido() {
        InvestmentSimulationRequestDTO request = new InvestmentSimulationRequestDTO();
        request.setValorInvestido(1000.0);
        request.setPtaz(12);
        request.setPerfilRisco("CONSERVADOR"); // exemplo
        request.setProdutoId(1L);              // precisa existir no banco de teste

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/simular-investimento")
                .then()
                .statusCode(200)
                .body("valorFinal", greaterThan(1000.0F))
                .body("produtoId", equalTo(1));
    }
}
