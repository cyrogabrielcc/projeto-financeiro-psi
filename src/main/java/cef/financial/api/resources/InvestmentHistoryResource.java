package cef.financial.api.resources;

import cef.financial.domain.dto.InvestmentHistoryResponseDTO;
import cef.financial.domain.model.InvestmentHistory;
import cef.financial.domain.repository.InvestmentHistoryRepository;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.Content;

import java.util.List;

@Path("/investimentos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Investimentos", description = "Consulta de histórico de investimentos de clientes")
public class InvestmentHistoryResource {

    @Inject
    InvestmentHistoryRepository investmentHistoryRepository;

    public InvestmentHistoryResource() {}

    public InvestmentHistoryResource(InvestmentHistoryRepository investmentHistoryRepository) {
        this.investmentHistoryRepository = investmentHistoryRepository;
    }

    @GET
    @Path("/{clienteId}")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Consultar histórico de investimentos",
            description = "Retorna o histórico completo de investimentos associados ao cliente informado."
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
            description = "Cliente não possui histórico ou não foi encontrado"
    )
    @APIResponse(
            responseCode = "403",
            description = "Acesso negado — usuário não possui permissão para consultar"
    )
    public List<InvestmentHistoryResponseDTO> historicoInvestimentos(@PathParam("clienteId") Long clienteId) {

        List<InvestmentHistory> history = investmentHistoryRepository.list("clienteId", clienteId);

        return history.stream().map(h -> {
            InvestmentHistoryResponseDTO dto = new InvestmentHistoryResponseDTO();
            dto.id = h.id;
            dto.tipo = h.tipo;
            dto.valor = h.valor;
            dto.rentabilidade = h.rentabilidade;
            dto.data = h.dataInvestimento;
            return dto;
        }).toList();
    }
}
