package cef.financial.api.resources;

import cef.financial.domain.dto.CustomerResponseDTO;
import cef.financial.domain.model.Customer;
import cef.financial.domain.repository.CustomerRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Clientes", description = "Endpoints relacionados aos clientes do sistema")
public class CustomerResource {

    @Inject
    CustomerRepository customerRepository;

    @GET
    @Path("/clientes")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Listar clientes",
            description = "Retorna a lista completa de clientes cadastrados no sistema."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de clientes retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CustomerResponseDTO.class)
            )
    )
    @APIResponse(
            responseCode = "403",
            description = "Acesso negado — usuário não possui as permissões necessárias."
    )
    public List<CustomerResponseDTO> listarClientes() {

        List<Customer> entities = customerRepository.listAll();

        return entities.stream()
                .map(c -> new CustomerResponseDTO(
                        c.id,
                        c.perfil,
                        c.criadoEm
                ))
                .toList();
    }
}
