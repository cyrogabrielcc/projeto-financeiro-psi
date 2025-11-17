package cef.financial.api.resources;

import cef.financial.domain.dto.InvestmentHistoryResponseDTO;
import cef.financial.domain.model.InvestmentHistory;
import cef.financial.domain.repository.InvestmentHistoryRepository;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/investimentos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class InvestmentHistoryResource {

    private static final Logger LOG = Logger.getLogger(InvestmentHistoryResource.class);

    @Inject
    InvestmentHistoryRepository investmentHistoryRepository;

    public InvestmentHistoryResource() {}

    public InvestmentHistoryResource(InvestmentHistoryRepository investmentHistoryRepository) {
        this.investmentHistoryRepository = investmentHistoryRepository;
    }

    @GET
    @Path("/{clienteId}")
    @RolesAllowed({"user", "admin"})
    public List<InvestmentHistoryResponseDTO> historicoInvestimentos(@PathParam("clienteId") Long clienteId) {

        List<InvestmentHistory> history =
                investmentHistoryRepository.list("clienteId", clienteId);

        if (history == null || history.isEmpty()) {
            LOG.infof("Cliente %d não possui histórico de investimentos. Retornando lista vazia.", clienteId);
            return List.of();
        }

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
