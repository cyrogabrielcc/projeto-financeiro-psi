package cef.financial.api.resources;

import cef.financial.domain.dto.InvestmentSimulationRequestDTO;
import cef.financial.domain.dto.InvestmentSimulationResponseDTO;
import cef.financial.domain.dto.SimulationByProductDayResponseDTO;
import cef.financial.domain.dto.SimulationHistoryResponseDTO;
import cef.financial.domain.model.InvestmentSimulation;
import cef.financial.domain.service.InvestmentSimulationService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated

public class SimulationResource {

    @Inject
    InvestmentSimulationService simulationService;

    @POST
    @Path("/simular-investimento")
    @RolesAllowed({"user", "admin"})
    public Response simularInvestimento(@Valid InvestmentSimulationRequestDTO request) {
        InvestmentSimulationResponseDTO response = simulationService.simulate(request);
        return Response.ok(response).build();
    }

    // 2. GET /simulacoes
    @GET
    @Path("/simulacoes")
    //@RolesAllowed({"user", "admin"})
    public List<SimulationHistoryResponseDTO> listarSimulacoes() {
        return InvestmentSimulation.<InvestmentSimulation>listAll().stream()
                .map(sim -> {
                    SimulationHistoryResponseDTO dto = new SimulationHistoryResponseDTO();
                    dto.id = sim.id;
                    dto.clienteId = sim.clienteId;
                    dto.produto = sim.produto.nome;
                    dto.valorInvestido = sim.valorInvestido;
                    dto.valorFinal = sim.valorFinal;
                    dto.prazoMeses = sim.prazoMeses;
                    dto.dataSimulacao = sim.dataSimulacao;
                    return dto;
                }).toList();
    }

    // 3. GET /simulacoes/por-produto-dia
    @GET
    @Path("/simulacoes/por-produto-dia")
    //@RolesAllowed({"user", "admin"})
    public List<SimulationByProductDayResponseDTO> simulacoesPorProdutoDia() {
        List<InvestmentSimulation> sims = InvestmentSimulation.listAll();

        Map<String, Map<LocalDate, List<InvestmentSimulation>>> grouped =
                sims.stream().collect(Collectors.groupingBy(
                        sim -> sim.produto.nome,
                        Collectors.groupingBy(sim -> sim.dataSimulacao.toLocalDate())
                ));

        return grouped.entrySet().stream()
                .flatMap(entryProduto -> entryProduto.getValue().entrySet().stream()
                        .map(entryDia -> {
                            String produto = entryProduto.getKey();
                            LocalDate dia = entryDia.getKey();
                            List<InvestmentSimulation> list = entryDia.getValue();

                            SimulationByProductDayResponseDTO dto = new SimulationByProductDayResponseDTO();
                            dto.produto = produto;
                            dto.data = dia;
                            dto.quantidadeSimulacoes = list.size();
                            dto.mediaValorFinal = list.stream()
                                    .mapToDouble(sim -> sim.valorFinal)
                                    .average()
                                    .orElse(0.0);
                            return dto;
                        })
                ).toList();
    }
}
