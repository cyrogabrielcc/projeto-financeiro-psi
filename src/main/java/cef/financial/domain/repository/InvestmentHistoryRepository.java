package cef.financial.domain.repository;

import cef.financial.domain.model.InvestmentHistory;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InvestmentHistoryRepository implements PanacheRepository<InvestmentHistory> {
}