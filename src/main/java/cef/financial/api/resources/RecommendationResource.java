package cef.financial.api.resources;

import cef.financial.domain.dto.InvestmentProductResponseDTO;
import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.service.RecommendationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/produtos-recomendados")
@Produces(MediaType.APPLICATION_JSON)
public class RecommendationResource {

    @Inject
    RecommendationService recommendationService;

    @GET
    @Path("/{perfil}")
    public List<InvestmentProductResponseDTO> produtosRecomendados(@PathParam("perfil") String perfil) {
        return recommendationService.recommendByProfile(perfil)
                .stream()
                .map(InvestmentProductResponseDTO::from)
                .toList();
    }
}
