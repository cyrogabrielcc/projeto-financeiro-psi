package cef.financial.domain.repository;

import cef.financial.domain.model.InvestmentSimulation;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InvestmentSimulationRepository implements PanacheRepository<InvestmentSimulation> {
}