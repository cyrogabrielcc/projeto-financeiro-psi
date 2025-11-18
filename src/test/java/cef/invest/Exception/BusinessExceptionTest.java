package cef.invest.Exception;


import cef.financial.domain.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void deveCriarExcecaoComStatusPadrao400() {
        String mensagem = "Mensagem de erro padrão";
        BusinessException exception = new BusinessException(mensagem);

        assertEquals(mensagem, exception.getMessage());
        assertEquals(400, exception.getStatus());
    }

    @Test
    void deveCriarExcecaoComStatusCustomizado() {
        String mensagem = "Recurso não encontrado";
        int status = 404;
        BusinessException exception = new BusinessException(mensagem, status);

        assertEquals(mensagem, exception.getMessage());
        assertEquals(status, exception.getStatus());
    }
}