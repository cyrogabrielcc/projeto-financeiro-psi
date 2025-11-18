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

    // ================== STARTUP ==================
    @Transactional
    void onStart(@Observes StartupEvent ev) {
        LOG.info("DatabaseSeeder: iniciando seed...");
        seed();
        LOG.info("DatabaseSeeder: seed finalizado.");
    }

    void seed() {
        seedProducts();
        seedCustomers();
        seedSimulations();
        seedInvestmentHistory();
    }

    // ================== PRODUTOS ==================
    private void seedProducts() {
        long productCount = productRepository.count();

        if (productCount > 0) {
            LOG.infof("DatabaseSeeder: já existem %d produtos cadastrados. Seed de produtos ignorado.", productCount);
            return;
        }

        LOG.info("DatabaseSeeder: nenhum produto encontrado. Populando produtos de teste...");

        long nextId = 1L; // IDs vindo do seed

        // 1) CDB 100% CDI
        InvestmentProduct cdb100 = new InvestmentProduct();
        cdb100.id = nextId++; // << ID DEFINIDO NO SEED
        cdb100.nome = "CDB 100% CDI";
        cdb100.tipo = "CDB";
        cdb100.risco = "BAIXO";
        cdb100.liquidezDias = 1;
        cdb100.prazoMinMeses = 6;
        cdb100.prazoMaxMeses = 36;
        cdb100.rentabilidadeAnual = 0.13;
        productRepository.persist(cdb100);

        // 2) CDB 120% CDI
        InvestmentProduct cdb120 = new InvestmentProduct();
        cdb120.id = nextId++;
        cdb120.nome = "CDB 120% CDI";
        cdb120.tipo = "CDB";
        cdb120.risco = "MÉDIO";
        cdb120.liquidezDias = 30;
        cdb120.prazoMinMeses = 12;
        cdb120.prazoMaxMeses = 48;
        cdb120.rentabilidadeAnual = 0.16;
        productRepository.persist(cdb120);

        // 3) Tesouro Selic
        InvestmentProduct tesouroSelic = new InvestmentProduct();
        tesouroSelic.id = nextId++;
        tesouroSelic.nome = "Tesouro Selic 2029";
        tesouroSelic.tipo = "TESOURO";
        tesouroSelic.risco = "BAIXO";
        tesouroSelic.liquidezDias = 1;
        tesouroSelic.prazoMinMeses = 24;
        tesouroSelic.prazoMaxMeses = 60;
        tesouroSelic.rentabilidadeAnual = 0.11;
        productRepository.persist(tesouroSelic);

        // 4) Fundo multimercado
        InvestmentProduct fundoMulti = new InvestmentProduct();
        fundoMulti.id = nextId++;
        fundoMulti.nome = "Fundo Multimercado XYZ";
        fundoMulti.tipo = "Fundo Multimercado";
        fundoMulti.risco = "ALTO";
        fundoMulti.liquidezDias = 30;
        fundoMulti.prazoMinMeses = 12;
        fundoMulti.prazoMaxMeses = 0; // 0 = sem limite
        fundoMulti.rentabilidadeAnual = 0.18;
        productRepository.persist(fundoMulti);

        // 5) LCI Imobiliária
        InvestmentProduct lci = new InvestmentProduct();
        lci.id = nextId++;
        lci.nome = "LCI Imobiliária 95% CDI";
        lci.tipo = "LCI";
        lci.risco = "BAIXO";
        lci.liquidezDias = 90;
        lci.prazoMinMeses = 12;
        lci.prazoMaxMeses = 36;
        lci.rentabilidadeAnual = 0.125;
        productRepository.persist(lci);

        // 6) Debênture Incentivada
        InvestmentProduct debenture = new InvestmentProduct();
        debenture.id = nextId++;
        debenture.nome = "Debênture Incentivada ABC";
        debenture.tipo = "Debênture";
        debenture.risco = "MÉDIO";
        debenture.liquidezDias = 0; // sem liquidez diária
        debenture.prazoMinMeses = 36;
        debenture.prazoMaxMeses = 84;
        debenture.rentabilidadeAnual = 0.145;
        productRepository.persist(debenture);

        productRepository.flush();

        LOG.infof("DatabaseSeeder: inseridos %d produtos de teste.", productRepository.count());
    }

    // ================== CLIENTES ==================
    private void seedCustomers() {
        long customerCount = customerRepository.count();

        // Em vez de ignorar sempre, garantimos que existam pelo menos 13
        if (customerCount >= 13) {
            LOG.infof("DatabaseSeeder: já existem %d clientes cadastrados. Nenhum novo cliente será criado.", customerCount);
            return;
        }

        LOG.infof("DatabaseSeeder: existem %d clientes. Criando até 13...", customerCount);

        OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.UTC);

        // Começa do próximo id disponível até 13
        long startId = customerCount + 1;
        if (startId < 1L) startId = 1L; // segurança

        for (long id = startId; id <= 13L; id++) {
            Customer c = new Customer();
            c.id = id;  // ID MANUAL (mesma lógica que você já usava)
            c.perfil = switch ((int) (id % 3)) {
                case 1 -> "CONSERVADOR";
                case 2 -> "MODERADO";
                default -> "ARROJADO";
            };
            c.criadoEm = agora.minusDays(id * 2);
            customerRepository.persist(c);
        }

        customerRepository.flush();

        LOG.infof("DatabaseSeeder: agora existem %d clientes cadastrados.", customerRepository.count());
    }

    // ================== SIMULAÇÕES ==================
    private void seedSimulations() {
        List<Customer> customers = customerRepository.listAll();
        List<InvestmentProduct> products = productRepository.listAll();

        if (customers.isEmpty() || products.isEmpty()) {
            LOG.warn("DatabaseSeeder: não há clientes ou produtos suficientes para criar simulações de teste.");
            return;
        }

        LOG.info("DatabaseSeeder: garantindo 10 simulações para cada cliente...");

        OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.UTC);
        int totalCriadas = 0;

        for (Customer customer : customers) {
            // conta quantas simulações esse cliente já tem
            long existentes = simulationRepository.count("clienteId", customer.id);

            if (existentes >= 10) {
                LOG.debug("Cliente " + customer.id + " já possui " + existentes + " simulações. Nenhuma nova será criada.");
                continue;
            }

            // cria até completar 10
            for (int i = (int) existentes + 1; i <= 10; i++) {

                InvestmentProduct product =
                        products.get((int) ((customer.id + i) % products.size()));

                double valorBase = 1000.0 + (customer.id * 100.0) + (i * 50.0);
                int prazoMeses = 6 + (i % 12); // entre 7 e 17 meses

                OffsetDateTime dataSimulacao =
                        agora.minusDays(customer.id * 2 + i);

                createSimulation(customer, product, valorBase, prazoMeses, dataSimulacao);
                totalCriadas++;
            }
        }

        LOG.infof("DatabaseSeeder: criadas %d novas simulações. Total na base: %d",
                totalCriadas, simulationRepository.count());
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

        Customer c1 = customers.get(0);
        Customer c2 = customers.size() > 1 ? customers.get(1) : c1;

        InvestmentHistory h1 = new InvestmentHistory();
        h1.clienteId = c1.id;
        h1.tipo = "CDB";
        h1.valor = 5000.00;
        h1.rentabilidade = 0.12;
        h1.dataInvestimento = LocalDate.of(2025, 1, 15);
        h1.persist();

        InvestmentHistory h2 = new InvestmentHistory();
        h2.clienteId = c1.id;
        h2.tipo = "Fundo Multimercado";
        h2.valor = 3000.00;
        h2.rentabilidade = 0.08;
        h2.dataInvestimento = LocalDate.of(2025, 3, 10);
        h2.persist();

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
