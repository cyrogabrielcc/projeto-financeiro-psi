package cef.financial.api.resources;

import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.repository.InvestmentProductRepository;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ProductResource {

    @Inject
    InvestmentProductRepository productRepository;

    @GET
    @RolesAllowed({"user", "admin"})  // <-- remova se quiser endpoint aberto
    public List<InvestmentProduct> listarProdutos() {
        return productRepository.listAll();
    }
}
