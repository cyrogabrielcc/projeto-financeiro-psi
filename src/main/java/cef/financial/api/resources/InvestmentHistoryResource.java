package cef.financial.api.resources;

import cef.financial.domain.dto.InvestmentHistoryResponseDTO;
import cef.financial.domain.model.InvestmentHistory;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/investimentos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
//@Authenticated
//@SecurityRequirement(name = "bearerAuth")
public class InvestmentHistoryResource {

    @GET
    @Path("/{clienteId}")
    @RolesAllowed({"user", "admin"})
    public List<InvestmentHistoryResponseDTO> historicoInvestimentos(@PathParam("clienteId") Long clienteId) {
        List<InvestmentHistory> history = InvestmentHistory.list("clienteId", clienteId);
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
