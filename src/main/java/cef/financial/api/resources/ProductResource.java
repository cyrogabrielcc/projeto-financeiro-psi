package cef.financial.api.resources;

import cef.financial.domain.dto.CreateInvestmentProductRequestDTO;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.repository.InvestmentProductRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/produtos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    InvestmentProductRepository productRepository;

    // GET /produtos  -> ordenado por id
    @GET
    @RolesAllowed({"user", "admin"})
    public List<InvestmentProduct> listarProdutos() {
        return productRepository.listAll();
    }
}
