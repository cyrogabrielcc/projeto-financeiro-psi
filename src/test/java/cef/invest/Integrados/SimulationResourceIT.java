package cef.invest.Integrados;

import cef.financial.domain.repository.InvestmentSimulationRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
@QuarkusTest
class SimulationResourceIT {

    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    void simularInvestimento_DeveRetornar200() {
        String requestJson = """
            {
              "produtoId": 1,
              "clienteId": 1,
              "valor": 5000.0,
              "prazoMeses": 24,
              "tipoProduto": "CDB"
            }
            """;

        given()
                .contentType("application/json")
                .body(requestJson)
                .when()
                .post("/simular-investimento")
                .then()
                .statusCode(200);
    }
    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    void simularInvestimento_SemTipoProduto_DeveRetornar400() {
        String json = """
        {
          "produtoId": 1,
          "clienteId": 1,
          "valor": 5000.0,
          "prazoMeses": 24
        }
        """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/simular-investimento")
                .then()
                .statusCode(400);
    }
    @Test
    @TestSecurity(user = "cliente", roles = {"cliente"})
    void simularInvestimento_ComRoleInvalida_DeveRetornar403() {
        String json = """
        {
          "produtoId": 1,
          "clienteId": 1,
          "valor": 5000.0,
          "prazoMeses": 24,
          "tipoProduto": "CDB"
        }
        """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/simular-investimento")
                .then()
                .statusCode(403)
                .body("message", org.hamcrest.Matchers.equalTo(
                        "Acesso negado. O token não possui as permissões necessárias para este recurso."
                ));
    }
    @Test
    void simularInvestimento_SemToken_DeveRetornar401() {
        String json = """
        {
          "produtoId": 1,
          "clienteId": 1,
          "valor": 5000.0,
          "prazoMeses": 24,
          "tipoProduto": "CDB"
        }
        """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/simular-investimento")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "user", roles = {"user"})
    void simularInvestimento_UserRole_DeveRetornar200() {
        String json = """
        {
          "produtoId": 1,
          "clienteId": 1,
          "valor": 5000.0,
          "prazoMeses": 24,
          "tipoProduto": "CDB"
        }
        """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/simular-investimento")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    void simularInvestimento_PrazoForaDoPermitido_DeveRetornarErroRegra() {
        String json = """
        {
          "produtoId": 1,
          "clienteId": 1,
          "valor": 5000.0,
          "prazoMeses": 999,
          "tipoProduto": "CDB"
        }
        """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/simular-investimento")
                .then()
                .statusCode(400); // ou 422, conforme você definiu
    }
    @Inject
    InvestmentSimulationRepository simulationRepository;

    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    @Transactional
    void simularInvestimento_DevePersistirSimulacao() {
        String json = """
            {
              "produtoId": 1,
              "clienteId": 1,
              "valor": 7000.0,
              "prazoMeses": 12,
              "tipoProduto": "CDB"
            }
            """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/simular-investimento")
                .then()
                .statusCode(200);

        // 🔴 ERRADO (gera o erro que você viu):
        // long count = simulationRepository.count("valorInicial", 7000.0);

        // ✅ CERTO – usando o nome do atributo da entidade:
        long count = simulationRepository.count("valorInvestido", 7000.0);
        // ou: long count = simulationRepository.count("valorInvestido = ?1", 7000.0);

        Assertions.assertTrue(count > 0);
    }

}