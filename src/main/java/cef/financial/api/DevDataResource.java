package cef.financial.api;

import cef.financial.domain.model.InvestmentProduct;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class DevDataResource {

    @POST
    @Path("/dev/popular-produtos")
    @Transactional
    @RolesAllowed({"admin"})
    public Response popularProdutosDev() {
        if (InvestmentProduct.count() == 0) {
            InvestmentProduct cdb = new InvestmentProduct();
            cdb.nome = "CDB Caixa 2026";
            cdb.tipo = "CDB";
            cdb.rentabilidadeAnual = 0.12;
            cdb.risco = "Baixo";
            cdb.prazoMinimoMeses = 6;
            cdb.prazoMaximoMeses = 24;
            cdb.liquidezDias = 1;
            cdb.perfilRecomendado = "Conservador";
            cdb.persist();

            InvestmentProduct fundo = new InvestmentProduct();
            fundo.nome = "Fundo Batatinha Doce da Nubank";
            fundo.tipo = "Fundo";
            fundo.rentabilidadeAnual = 0.18;
            fundo.risco = "Alto";
            fundo.prazoMinimoMeses = 3;
            fundo.prazoMaximoMeses = 60;
            fundo.liquidezDias = 30;
            fundo.perfilRecomendado = "Agressivo";
            fundo.persist();
        }
        return Response.ok().build();
    }
}
