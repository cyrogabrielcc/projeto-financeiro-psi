package cef.financial.domain.repository;

import cef.financial.domain.model.InvestmentProduct;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InvestmentProductRepository implements PanacheRepository<InvestmentProduct> {
    // Métodos específicos podem ser adicionados aqui, ex:
    // public List<InvestmentProduct> findByTipo(String tipo) { ... }
}
