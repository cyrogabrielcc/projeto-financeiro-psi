package cef.financial.api.resources;

import cef.financial.domain.dto.InvestmentProductResponseDTO;
import cef.financial.domain.service.RecommendationService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Path("/produtos-recomendados")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recomendações", description = "Recomenda produtos financeiros com base no perfil do cliente")
public class RecommendationResource {

    @Inject
    RecommendationService recommendationService;

    @GET
    @Path("/{perfil}")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Listar produtos recomendados",
            description = "Retorna uma lista de produtos de investimento recomendados com base no perfil informado. Perfis esperados: CONSERVADOR, MODERADO, AGRESSIVO."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de produtos recomendados retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InvestmentProductResponseDTO.class)
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Perfil informado é inválido"
    )
    @APIResponse(
            responseCode = "403",
            description = "Acesso negado — usuário não possui permissão"
    )
    public List<InvestmentProductResponseDTO> produtosRecomendados(@PathParam("perfil") String perfil) {
        return recommendationService.recommendByProfile(perfil)
                .stream()
                .map(InvestmentProductResponseDTO::from)
                .toList();
    }
}
