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

    public SimulationResource() {}

    // construtor para testes
    public SimulationResource(InvestmentSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @POST
    @Path("/simular-investimento")
    @RolesAllowed({"user", "admin"})
    public Response simularInvestimento(@Valid InvestmentSimulationRequestDTO request) {
        InvestmentSimulationResponseDTO response = simulationService.simulate(request);
        return Response.ok(response).build();
    }

    @GET
    @Path("/simulacoes")
    public List<SimulationHistoryResponseDTO> listarSimulacoes() {

        List<InvestmentSimulation> sims = simulationService.listAllSimulations();

        return sims.stream().map(sim -> {
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

    @GET
    @Path("/simulacoes/por-produto-dia")
    public List<SimulationByProductDayResponseDTO> simulacoesPorProdutoDia() {

        List<InvestmentSimulation> sims = simulationService.listAllSimulations();

        Map<String, Map<LocalDate, List<InvestmentSimulation>>> grouped =
                sims.stream().collect(Collectors.groupingBy(
                        s -> s.produto.nome,
                        Collectors.groupingBy(s -> s.dataSimulacao.toLocalDate())
                ));

        return grouped.entrySet().stream()
                .flatMap(entryProduto -> entryProduto.getValue().entrySet().stream()
                        .map(entryDia -> {
                            String produto = entryProduto.getKey();
                            LocalDate dia = entryDia.getKey();
                            List<InvestmentSimulation> lista = entryDia.getValue();

                            SimulationByProductDayResponseDTO dto = new SimulationByProductDayResponseDTO();
                            dto.produto = produto;
                            dto.data = dia;
                            dto.quantidadeSimulacoes = lista.size();
                            dto.mediaValorFinal = lista.stream()
                                    .mapToDouble(s -> s.valorFinal)
                                    .average()
                                    .orElse(0.0);

                            return dto;
                        })
                ).toList();
    }
}
