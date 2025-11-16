package cef.financial.api.resources;

import cef.financial.domain.dto.CustomerResponseDTO;
import cef.financial.domain.model.Customer;
import cef.financial.domain.repository.CustomerRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerRepository customerRepository;

    @GET
    @Path("/clientes")
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
