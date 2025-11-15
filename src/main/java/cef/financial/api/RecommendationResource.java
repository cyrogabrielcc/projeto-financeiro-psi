package cef.financial.api;

import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.service.RecommendationService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import java.util.List;

@Path("/produtos-recomendados")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
//@Authenticated
//@SecurityRequirement(name = "bearerAuth")
public class RecommendationResource {

    @Inject
    RecommendationService recommendationService;

    @GET
    @Path("/produtos-recomendados/{perfil}")
//    @RolesAllowed({"user", "admin"})
    public List<InvestmentProduct> produtosRecomendados(@PathParam("perfil") String perfil) {
        return recommendationService.recommendByProfile(perfil);
    }
}
