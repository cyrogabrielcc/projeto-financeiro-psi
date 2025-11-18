package cef.financial.api.resources;

import cef.financial.domain.dto.InvestmentProductResponseDTO;
import cef.financial.domain.service.RecommendationService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

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
    @Operation(
            summary = "Lista produtos recomendados pelo perfil do cliente",
            description = """
                Retorna os produtos adequados ao perfil informado: 
                CONSERVADOR, MODERADO ou ARROJADO. 
                Produtos são filtrados com base em seu nível de risco.
            """
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de produtos recomendados retornada com sucesso",
            content = @Content(
                    schema = @Schema(implementation = InvestmentProductResponseDTO.class)
            )
    )
    @APIResponse(
            responseCode = "403",
            description = "Acesso negado — usuário não autenticado"
    )
    @APIResponse(
            responseCode = "404",
            description = "Perfil informado é inválido"
    )
    public List<InvestmentProductResponseDTO> produtosRecomendados(
            @Parameter(
                    description = "Perfil do cliente (CONSERVADOR, MODERADO ou ARROJADO)",
                    required = true
            )
            @PathParam("perfil") String perfil
    ) {
        List<InvestmentProductResponseDTO> result =
                recommendationService.recommendByProfile(perfil)
                        .stream()
                        .map(InvestmentProductResponseDTO::from)
                        .toList();

        if (result.isEmpty()) {
            throw new NotFoundException("Nenhum produto encontrado para o perfil: " + perfil);
        }

        return result;
    }
}
