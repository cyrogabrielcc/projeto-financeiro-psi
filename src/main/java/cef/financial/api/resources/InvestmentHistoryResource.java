package cef.financial.api.resources;

import cef.financial.domain.dto.InvestmentHistoryResponseDTO;
import cef.financial.domain.model.InvestmentHistory;
import cef.financial.domain.repository.InvestmentHistoryRepository;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/investimentos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class InvestmentHistoryResource {

    @Inject
    InvestmentHistoryRepository investmentHistoryRepository;

    // construtor padrão para CDI
    public InvestmentHistoryResource() {
    }

    // construtor para testes unitários
    public InvestmentHistoryResource(InvestmentHistoryRepository investmentHistoryRepository) {
        this.investmentHistoryRepository = investmentHistoryRepository;
    }

    @GET
    @Path("/{clienteId}")
    @RolesAllowed({"user", "admin"})
    public List<InvestmentHistoryResponseDTO> historicoInvestimentos(@PathParam("clienteId") Long clienteId) {
        // antes: InvestmentHistory.list("clienteId", clienteId);
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
