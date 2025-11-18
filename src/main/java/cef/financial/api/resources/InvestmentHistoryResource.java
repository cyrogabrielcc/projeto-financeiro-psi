package cef.financial.api.resources;

import cef.financial.domain.dto.InvestmentHistoryResponseDTO;
import cef.financial.domain.model.InvestmentSimulation;
import cef.financial.domain.repository.InvestmentSimulationRepository;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/investimentos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class InvestmentHistoryResource {

    private static final Logger LOG = Logger.getLogger(InvestmentHistoryResource.class);

    @Inject
    InvestmentSimulationRepository simulationRepository;

    public InvestmentHistoryResource() {}

    public InvestmentHistoryResource(InvestmentSimulationRepository simulationRepository) {
        this.simulationRepository = simulationRepository;
    }

    @GET
    @Path("/{clienteId}")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Lista o histórico de investimentos do cliente",
            description = """
                Retorna o histórico consolidado de investimentos do cliente.
                O histórico é derivado das simulações já realizadas.
                Inclui tipo do produto, valor investido, rentabilidade 
                e data original da simulação.
            """
    )
    @APIResponse(
            responseCode = "200",
            description = "Histórico retornado com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InvestmentHistoryResponseDTO.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Cliente não encontrado"
    )
    @APIResponse(
            responseCode = "403",
            description = "Acesso negado — usuário não autenticado"
    )
    public List<InvestmentHistoryResponseDTO> historicoInvestimentos(
            @Parameter(description = "ID do cliente", required = true)
            @PathParam("clienteId") Long clienteId
    ) {

        List<InvestmentSimulation> sims = simulationRepository.list("clienteId", clienteId);

        if (sims == null || sims.isEmpty()) {
            throw new NotFoundException("Cliente não existente na base");
        }

        return sims.stream().map(s -> {
            InvestmentHistoryResponseDTO dto = new InvestmentHistoryResponseDTO();
            dto.id = s.id;
            dto.tipo = (s.produto != null ? s.produto.tipo : null);
            dto.valor = s.valorInvestido > 0 ? s.valorInvestido : 0.0;

            if (s.valorInvestido > 0) {
                dto.rentabilidade = (s.valorFinal - s.valorInvestido) / s.valorInvestido;
            } else {
                dto.rentabilidade = 0.0;
            }

            dto.data = s.dataSimulacao != null ? s.dataSimulacao.toLocalDate() : null;

            return dto;
        }).toList();
    }
}
