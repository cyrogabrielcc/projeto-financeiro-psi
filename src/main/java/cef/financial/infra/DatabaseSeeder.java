package cef.financial.infra;

import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.model.InvestmentSimulation;
import cef.financial.domain.model.Customer;
import cef.financial.domain.model.InvestmentHistory;
import cef.financial.domain.repository.InvestmentProductRepository;
import cef.financial.domain.repository.InvestmentSimulationRepository;
import cef.financial.domain.repository.CustomerRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class DatabaseSeeder {

    private static final Logger LOG = Logger.getLogger(DatabaseSeeder.class);

    @Inject
    InvestmentProductRepository productRepository;

    @Inject
    CustomerRepository customerRepository;

    @Inject
    InvestmentSimulationRepository simulationRepository;

    // Chamado automaticamente quando o Quarkus sobe
    void onStart(@Observes StartupEvent ev) {
        seed();
    }

    @Transactional
    void seed() {
        seedProducts();
        seedCustomers();
        seedSimulations();
        seedInvestmentHistory(); // <<< NOVO
    }

    // ================== PRODUTOS ==================
    private void seedProducts() {
        long productCount = productRepository.count();

        if (productCount > 0) {
            LOG.infof("DatabaseSeeder: já existem %d produtos cadastrados. Seed de produtos ignorado.", productCount);
            return;
        }

        LOG.info("DatabaseSeeder: nenhum produto encontrado. Populando produtos de teste...");

        InvestmentProduct cdb100 = new InvestmentProduct();
        cdb100.nome = "CDB 100% CDI";
        cdb100.tipo = "CDB";
        cdb100.risco = "BAIXO";
        cdb100.liquidezDias = 1;
        cdb100.prazoMinMeses = 6;
        cdb100.prazoMaxMeses = 36;
        cdb100.rentabilidadeAnual = 0.13;
        productRepository.persist(cdb100);

        InvestmentProduct cdb120 = new InvestmentProduct();
        cdb120.nome = "CDB 120% CDI";
        cdb120.tipo = "CDB";
        cdb120.risco = "MÉDIO";
        cdb120.liquidezDias = 30;
        cdb120.prazoMinMeses = 12;
        cdb120.prazoMaxMeses = 48;
        cdb120.rentabilidadeAnual = 0.16;
        productRepository.persist(cdb120);

        InvestmentProduct tesouroSelic = new InvestmentProduct();
        tesouroSelic.nome = "Tesouro Selic 2029";
        tesouroSelic.tipo = "TESOURO";
        tesouroSelic.risco = "BAIXO";
        tesouroSelic.liquidezDias = 1;
        tesouroSelic.prazoMinMeses = 24;
        tesouroSelic.prazoMaxMeses = 60;
        tesouroSelic.rentabilidadeAnual = 0.11;
        productRepository.persist(tesouroSelic);

        InvestmentProduct fundoMulti = new InvestmentProduct();
        fundoMulti.nome = "Fundo Multimercado XYZ";
        fundoMulti.tipo = "Fundo Multimercado";
        fundoMulti.risco = "ALTO";
        fundoMulti.liquidezDias = 30;
        fundoMulti.prazoMinMeses = 12;
        fundoMulti.prazoMaxMeses = 0; // 0 = sem limite
        fundoMulti.rentabilidadeAnual = 0.18;
        productRepository.persist(fundoMulti);

        LOG.infof("DatabaseSeeder: inseridos %d produtos de teste.", productRepository.count());
    }

    // ================== CLIENTES ==================
    private void seedCustomers() {
        long customerCount = customerRepository.count();

        if (customerCount > 0) {
            LOG.infof("DatabaseSeeder: já existem %d clientes cadastrados. Seed de clientes ignorado.", customerCount);
            return;
        }

        LOG.info("DatabaseSeeder: nenhum cliente encontrado. Populando clientes de teste...");

        OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.UTC);

        Customer c1 = new Customer();
        c1.rendaMensal = 5000.0;
        c1.perfil = "CONSERVADOR";
        c1.criadoEm = agora.minusMonths(6);
        customerRepository.persist(c1);

        Customer c2 = new Customer();
        c2.rendaMensal = 12000.0;
        c2.perfil = "MODERADO";
        c2.criadoEm = agora.minusMonths(3);
        customerRepository.persist(c2);

        Customer c3 = new Customer();
        c3.rendaMensal = 25000.0;
        c3.perfil = "ARROJADO";
        c3.criadoEm = agora.minusMonths(1);
        customerRepository.persist(c3);

        LOG.infof("DatabaseSeeder: inseridos %d clientes de teste.", customerRepository.count());
    }

    // ================== SIMULAÇÕES ==================
    private void seedSimulations() {
        long simCount = simulationRepository.count();

        if (simCount > 0) {
            LOG.infof("DatabaseSeeder: já existem %d simulações cadastradas. Seed de simulações ignorado.", simCount);
            return;
        }

        List<Customer> customers = customerRepository.listAll();
        List<InvestmentProduct> products = productRepository.listAll();

        if (customers.isEmpty() || products.isEmpty()) {
            LOG.warn("DatabaseSeeder: não há clientes ou produtos suficientes para criar simulações de teste.");
            return;
        }

        LOG.info("DatabaseSeeder: criando simulações de teste...");

        OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.UTC);

        double[] valoresBase = { 500.0, 1000.0, 2000.0, 5000.0, 10000.0, 20000.0 };
        int[] prazosMeses   = { 3,    6,     12,     18,     24,      36      };

        int criadas = 0;

        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);

            for (int j = 0; j < products.size(); j++) {
                InvestmentProduct product = products.get(j);

                for (int k = 0; k < valoresBase.length; k++) {
                    double valor = valoresBase[k]
                            + (i * 700.0)
                            + (j * 400.0);

                    int prazo = prazosMeses[k];

                    long diasAtras = (long) (i * 10 + j * 5 + k + 1);
                    OffsetDateTime dataSimulacao = agora.minusDays(diasAtras);

                    createSimulation(customer, product, valor, prazo, dataSimulacao);
                    criadas++;
                }
            }
        }

        LOG.infof("DatabaseSeeder: criadas %d simulações de teste. Total na base: %d",
                criadas, simulationRepository.count());
    }

    private void createSimulation(Customer customer,
                                  InvestmentProduct product,
                                  double valor,
                                  int prazoMeses,
                                  OffsetDateTime dataSimulacao) {

        double taxaAnual = product.rentabilidadeAnual != null ? product.rentabilidadeAnual : 0.10;
        double taxaMensal = Math.pow(1 + taxaAnual, 1.0 / 12.0) - 1.0;
        double valorFinal = valor * Math.pow(1 + taxaMensal, prazoMeses);

        InvestmentSimulation sim = new InvestmentSimulation();
        sim.clienteId = customer.id;
        sim.produto = product;
        sim.valorInvestido = valor;
        sim.valorFinal = valorFinal;
        sim.prazoMeses = prazoMeses;
        sim.dataSimulacao = dataSimulacao;

        simulationRepository.persist(sim);
    }

    // ================== HISTÓRICO DE INVESTIMENTOS ==================
    private void seedInvestmentHistory() {
        long historyCount = InvestmentHistory.count();

        if (historyCount > 0) {
            LOG.infof("DatabaseSeeder: já existem %d registros em HTE_INVESTMENT_HISTORY. Seed ignorado.", historyCount);
            return;
        }

        List<Customer> customers = customerRepository.listAll();
        if (customers.isEmpty()) {
            LOG.warn("DatabaseSeeder: sem clientes para criar histórico de investimentos.");
            return;
        }

        LOG.info("DatabaseSeeder: criando histórico de investimentos de teste...");

        Customer c1 = customers.get(0); // normalmente será id = 1 em base limpa
        Customer c2 = customers.size() > 1 ? customers.get(1) : c1;

        // Registro 1 – deve bater com o exemplo do response
        InvestmentHistory h1 = new InvestmentHistory();
        h1.clienteId = c1.id;
        h1.tipo = "CDB";
        h1.valor = 5000.00;
        h1.rentabilidade = 0.12;
        h1.dataInvestimento = LocalDate.of(2025, 1, 15);
        h1.persist();

        // Registro 2 – também para o mesmo cliente
        InvestmentHistory h2 = new InvestmentHistory();
        h2.clienteId = c1.id;
        h2.tipo = "Fundo Multimercado";
        h2.valor = 3000.00;
        h2.rentabilidade = 0.08;
        h2.dataInvestimento = LocalDate.of(2025, 3, 10);
        h2.persist();

        // Alguns registros extras para outro cliente
        InvestmentHistory h3 = new InvestmentHistory();
        h3.clienteId = c2.id;
        h3.tipo = "Tesouro Selic";
        h3.valor = 8000.00;
        h3.rentabilidade = 0.09;
        h3.dataInvestimento = LocalDate.of(2024, 11, 20);
        h3.persist();

        InvestmentHistory h4 = new InvestmentHistory();
        h4.clienteId = c2.id;
        h4.tipo = "CDB";
        h4.valor = 4000.00;
        h4.rentabilidade = 0.11;
        h4.dataInvestimento = LocalDate.of(2025, 2, 5);
        h4.persist();

        LOG.infof("DatabaseSeeder: criados %d registros de histórico de investimentos.", InvestmentHistory.count());
    }
}
