package cef.financial.domain.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {

        // 1) SE FOR WebApplicationException, RESPEITA O STATUS ORIGINAL
        if (exception instanceof WebApplicationException wae) {
            // loga se quiser
            LOG.warnf(wae, "Erro de negócio: %s", wae.getMessage());
            return wae.getResponse(); // <- AQUI MORA A MAGIA: mantém o 400, 404, 422 etc.
        }

        // 2) Qualquer outra exceção → 500
        LOG.error("Erro inesperado na aplicação", exception);

        var body = new SimpleErrorResponse(
                "Erro interno no servidor.",
                500
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    // DTOzinho simples só pra ter corpo de erro bonitinho
    public static class SimpleErrorResponse {
        public String mensagem;
        public int status;

        public SimpleErrorResponse(String mensagem, int status) {
            this.mensagem = mensagem;
            this.status = status;
        }
    }
}
