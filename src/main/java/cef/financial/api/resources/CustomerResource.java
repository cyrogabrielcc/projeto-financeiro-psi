package cef.financial.api.resources;

import cef.financial.domain.dto.CustomerResponseDTO;
import cef.financial.domain.model.Customer;
import cef.financial.domain.repository.CustomerRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "bearerAuth")
public class CustomerResource {

    @Inject
    CustomerRepository customerRepository;

    @GET
    @Path("/clientes")
    @RolesAllowed({"user", "admin"})
    public List<CustomerResponseDTO> listarClientes() {

        List<Customer> entities = customerRepository.listAll();

        return entities.stream()
                .map(c -> new CustomerResponseDTO(
                        c.id,          // ajusta se o campo for outro
                        c.perfil,
                        c.criadoEm
                ))
                .toList();
    }
}
