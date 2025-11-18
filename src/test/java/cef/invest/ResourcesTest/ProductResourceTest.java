package cef.invest.ResourcesTest;

import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.repository.InvestmentProductRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
public class ProductResourceTest {

    // Injetamos um Mock do repositório para não bater no banco de verdade
    @InjectMock
    InvestmentProductRepository productRepository;

    @Test
    @TestSecurity(user = "admin", roles = {"user"}) // Simula usuário com role permitida
    public void testListarProdutos_ComUsuarioAutorizado() {
        // 1. Preparar o cenário (Mock)
        InvestmentProduct produtoFake = new InvestmentProduct();
        // Defina ID ou nome se necessário, ex: produtoFake.setName("CDB");

        Mockito.when(productRepository.listAll())
                .thenReturn(List.of(produtoFake));

        // 2. Executar e Validar
        given()
                .when().get("/produtos")
                .then()
                .statusCode(200)
                .body("$", hasSize(1)); // Verifica se retornou 1 item no array JSON
    }

    @Test
    @TestSecurity(user = "admin_teste", roles = {"admin"}) // Simula admin (também permitido)
    public void testListarProdutos_ComAdminAutorizado() {
        Mockito.when(productRepository.listAll())
                .thenReturn(Collections.emptyList());

        given()
                .when().get("/produtos")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    public void testListarProdutos_SemAutenticacao() {
        // Tenta acessar sem @TestSecurity (anônimo)
        given()
                .when().get("/produtos")
                .then()
                .statusCode(401); // Deve retornar Unauthorized
    }

    @Test
    @TestSecurity(user = "hacker", roles = {"visitante"}) // Role não listada no @RolesAllowed
    public void testListarProdutos_ComRoleProibida() {
        given()
                .when().get("/produtos")
                .then()
                .statusCode(403); // Deve retornar Forbidden
    }

    @Test
    @TestSecurity(user = "usuario_teste", roles = {"user"})
    public void testListarProdutos_ErroNoBanco() {
        // Simula uma exceção no repositório
        Mockito.when(productRepository.listAll())
                .thenThrow(new RuntimeException("Erro de conexão"));

        given()
                .when().get("/produtos")
                .then()
                .statusCode(500); // Internal Server Error
    }
}