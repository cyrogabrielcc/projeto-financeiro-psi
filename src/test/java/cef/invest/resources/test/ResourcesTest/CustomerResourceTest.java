package cef.invest.resources.test.ResourcesTest;

import cef.financial.api.resources.CustomerResource;
import cef.financial.domain.dto.CustomerResponseDTO;
import cef.financial.domain.model.Customer;
import cef.financial.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CustomerResource com máxima cobertura.
 */
@ExtendWith(MockitoExtension.class)
class CustomerResourceTest {

    @Mock
    CustomerRepository customerRepository; // mock correto do repository

    @InjectMocks
    CustomerResource customerResource; // injeta o mock corretamente

    @Test
    void listarClientes_deveRetornarListaVaziaQuandoRepositorioNaoTemClientes() {
        // arrange
        when(customerRepository.listAll()).thenReturn(List.of());

        // act
        List<CustomerResponseDTO> result = customerResource.listarClientes();

        // assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(customerRepository, times(1)).listAll();
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void listarClientes_deveMapearCorretamenteOsCamposDoCustomerParaDTO() {

        Customer c1 = new Customer();
        c1.id = 1L;
        c1.perfil = "CONSERVADOR";
        c1.criadoEm = OffsetDateTime.parse("2025-01-01T10:00:00Z");

        Customer c2 = new Customer();
        c2.id = 2L;
        c2.perfil = "ARROJADO";
        c2.criadoEm = OffsetDateTime.parse("2025-02-01T15:30:00Z");

        when(customerRepository.listAll()).thenReturn(List.of(c1, c2));

        // act
        List<CustomerResponseDTO> result = customerResource.listarClientes();

        // assert
        assertEquals(2, result.size());

        CustomerResponseDTO dto1 = result.get(0);
        CustomerResponseDTO dto2 = result.get(1);

        assertEquals(1L, dto1.id);
        assertEquals("CONSERVADOR", dto1.perfil);
        assertEquals(c1.criadoEm, dto1.criadoEm);

        assertEquals(2L, dto2.id);
        assertEquals("ARROJADO", dto2.perfil);
        assertEquals(c2.criadoEm, dto2.criadoEm);

        verify(customerRepository, times(1)).listAll();
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void listarClientes_deveFuncionarQuandoCamposEstaoNulos() {

        Customer c = new Customer();
        c.id = null;
        c.perfil = null;
        c.criadoEm = null;

        when(customerRepository.listAll()).thenReturn(List.of(c));

        // act
        List<CustomerResponseDTO> result = customerResource.listarClientes();

        // assert
        assertEquals(1, result.size());
        CustomerResponseDTO dto = result.get(0);

        assertNull(dto.id);
        assertNull(dto.perfil);
        assertNull(dto.criadoEm);

        verify(customerRepository, times(1)).listAll();
        verifyNoMoreInteractions(customerRepository);
    }
}