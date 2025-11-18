package cef.financial.api.resources;

import cef.financial.domain.dto.InvestmentProductResponseDTO;
import cef.financial.domain.service.RecommendationService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/produtos-recomendados")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class RecommendationResource {

    @Inject
    RecommendationService recommendationService;

    @GET
    @Path("/{perfil}")
    @RolesAllowed({"user", "admin"})
    public List<InvestmentProductResponseDTO> produtosRecomendados(@PathParam("perfil") String perfil) {
        return recommendationService.recommendByProfile(perfil)
                .stream()
                .map(InvestmentProductResponseDTO::from)
                .toList();
    }
}
