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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Simulações", description = "Endpoints para simular investimentos e consultar histórico de simulações")
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
    @Operation(
            summary = "Simular investimento",
            description = "Recebe os dados da simulação (cliente, produto, valor, prazo etc.) e retorna o resultado projetado do investimento."
    )
    @APIResponse(
            responseCode = "200",
            description = "Simulação realizada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InvestmentSimulationResponseDTO.class)
            )
    )
    @APIResponse(
            responseCode = "422",
            description = "Erro de validação nos dados da simulação"
    )
    @APIResponse(
            responseCode = "403",
            description = "Acesso negado — usuário não possui permissão"
    )
    public Response simularInvestimento(@Valid InvestmentSimulationRequestDTO request) {
        InvestmentSimulationResponseDTO response = simulationService.simulate(request);
        return Response.ok(response).build();
    }

    @GET
    @Path("/simulacoes")
    @Operation(
            summary = "Listar simulações",
            description = "Retorna a lista de todas as simulações de investimento realizadas no sistema."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de simulações retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SimulationHistoryResponseDTO.class)
            )
    )
    @APIResponse(
            responseCode = "403",
            description = "Acesso negado — usuário não autenticado"
    )
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
    @Operation(
            summary = "Agregação de simulações por produto e dia",
            description = "Retorna estatísticas de simulações agregadas por produto e dia, incluindo quantidade de simulações e média do valor final."
    )
    @APIResponse(
            responseCode = "200",
            description = "Agregação retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SimulationByProductDayResponseDTO.class)
            )
    )
    @APIResponse(
            responseCode = "403",
            description = "Acesso negado — usuário não autenticado"
    )
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
