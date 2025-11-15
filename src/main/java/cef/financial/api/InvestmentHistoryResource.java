package cef.financial.api;

import cef.financial.domain.dto.InvestmentHistoryResponse;
import cef.financial.domain.model.InvestmentHistory;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class InvestmentHistoryResource {

    @GET
    @Path("/investimentos/{clienteId}")
    @RolesAllowed({"user", "admin"})
    public List<InvestmentHistoryResponse> historicoInvestimentos(@PathParam("clienteId") Long clienteId) {
        List<InvestmentHistory> history = InvestmentHistory.list("clienteId", clienteId);
        return history.stream().map(h -> {
            InvestmentHistoryResponse dto = new InvestmentHistoryResponse();
            dto.id = h.id;
            dto.tipo = h.tipo;
            dto.valor = h.valor;
            dto.rentabilidade = h.rentabilidade;
            dto.data = h.data;
            return dto;
        }).toList();
    }
}
