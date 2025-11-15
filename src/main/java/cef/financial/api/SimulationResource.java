package cef.financial.api;

import cef.financial.domain.dto.InvestmentSimulationRequest;
import cef.financial.domain.dto.InvestmentSimulationResponse;
import cef.financial.domain.dto.SimulationByProductDayResponse;
import cef.financial.domain.dto.SimulationHistoryResponse;
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

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class SimulationResource {

    @Inject
    InvestmentSimulationService simulationService;

    // 1. POST /simular-investimento
    @POST
    @Path("/simular-investimento")
    @RolesAllowed({"user", "admin"})
    public Response simularInvestimento(@Valid InvestmentSimulationRequest request) {
        InvestmentSimulationResponse response = simulationService.simulate(request);
        return Response.ok(response).build();
    }

    // 2. GET /simulacoes
    @GET
    @Path("/simulacoes")
    @RolesAllowed({"user", "admin"})
    public List<SimulationHistoryResponse> listarSimulacoes() {
        return InvestmentSimulation.<InvestmentSimulation>listAll().stream()
                .map(sim -> {
                    SimulationHistoryResponse dto = new SimulationHistoryResponse();
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
    @RolesAllowed({"user", "admin"})
    public List<SimulationByProductDayResponse> simulacoesPorProdutoDia() {
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

                            SimulationByProductDayResponse dto = new SimulationByProductDayResponse();
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
