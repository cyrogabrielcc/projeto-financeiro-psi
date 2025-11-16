package cef.financial.api.resources;

import cef.financial.domain.model.InvestmentProduct;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoints de apoio para ambiente de desenvolvimento.
 * Não faz INSERT na base (para evitar problemas de lock no SQLite).
 */
@Path("/dev")
@Produces(MediaType.APPLICATION_JSON)
public class DevDataResource {

    /**
     * GET /dev/produtos
     *
     * Retorna a lista de produtos cadastrados, apenas para conferência.
     */
    @GET
    @Path("/produtos")
    public List<InvestmentProduct> listarProdutos() {
        return InvestmentProduct.listAll();
    }

    /**
     * GET /dev/status
     *
     * Retorna informações simples sobre o estado da base de produtos.
     */
    @GET
    @Path("/status")
    public Map<String, Object> status() {
        long count = InvestmentProduct.count();

        Map<String, Object> status = new HashMap<>();
        status.put("qtdProdutos", count);
        status.put("mensagem", count > 0
                ? "Produtos carregados (via import.sql)."
                : "Nenhum produto encontrado. Verifique o arquivo import.sql.");
        return status;
    }
}