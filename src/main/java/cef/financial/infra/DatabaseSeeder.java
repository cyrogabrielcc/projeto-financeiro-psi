package cef.financial.infra;

import cef.financial.domain.model.InvestmentProduct;
import cef.financial.domain.model.InvestmentSimulation;
import cef.financial.domain.model.Customer;
import cef.financial.domain.repository.InvestmentProductRepository;
import cef.financial.domain.repository.InvestmentSimulationRepository;
import cef.financial.domain.repository.CustomerRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

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
    }

    // ================== PRODUTOS ==================
    private void seedProducts() {
        long productCount = productRepository.count();

        if (productCount > 0) {
            LOG.infof("DatabaseSeeder: já existem %d produtos cadastrados. Seed de produtos ignorado.", productCount);
            return;
        }

        LOG.info("DatabaseSeeder: nenhum produto encontrado. Populando produtos de teste...");

        // Produto 1 – CDB 100% CDI
        InvestmentProduct cdb100 = new InvestmentProduct();
        cdb100.nome = "CDB 100% CDI";
        cdb100.tipo = "CDB";
        cdb100.risco = "BAIXO";
        cdb100.liquidezDias = 1;
        cdb100.prazoMinMeses = 6;
        cdb100.prazoMaxMeses = 36;
        cdb100.rentabilidadeAnual = 0.13;
        productRepository.persist(cdb100);

        // Produto 2 – CDB 120% CDI
        InvestmentProduct cdb120 = new InvestmentProduct();
        cdb120.nome = "CDB 120% CDI";
        cdb120.tipo = "CDB";
        cdb120.risco = "MÉDIO";
        cdb120.liquidezDias = 30;
        cdb120.prazoMinMeses = 12;
        cdb120.prazoMaxMeses = 48;
        cdb120.rentabilidadeAnual = 0.16;
        productRepository.persist(cdb120);

        // Produto 3 – Tesouro Selic
        InvestmentProduct tesouroSelic = new InvestmentProduct();
        tesouroSelic.nome = "Tesouro Selic 2029";
        tesouroSelic.tipo = "TESOURO";
        tesouroSelic.risco = "BAIXO";
        tesouroSelic.liquidezDias = 1;
        tesouroSelic.prazoMinMeses = 24;
        tesouroSelic.prazoMaxMeses = 60;
        tesouroSelic.rentabilidadeAnual = 0.11;
        productRepository.persist(tesouroSelic);

        // Produto 4 – Fundo Multimercado
        InvestmentProduct fundoMulti = new InvestmentProduct();
        fundoMulti.nome = "Fundo Multimercado XYZ";
        fundoMulti.tipo = "FUNDO";
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

        // Vários cenários de valor e prazo para gerar bastante variedade
        double[] valoresBase = { 500.0, 1000.0, 2000.0, 5000.0, 10000.0, 20000.0 };
        int[] prazosMeses   = { 3,    6,     12,     18,     24,      36      };

        long inicial = simCount;
        int criadas = 0;

        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);

            for (int j = 0; j < products.size(); j++) {
                InvestmentProduct product = products.get(j);

                for (int k = 0; k < valoresBase.length; k++) {
                    double valorBase = valoresBase[k];

                    // Pequenas variações por cliente e produto, pra não ficar tudo igual
                    double valor = valorBase
                            + (i * 700.0)   // variação por cliente
                            + (j * 400.0);  // variação por produto

                    int prazo = prazosMeses[k];

                    // Espalha as datas no tempo: quanto mais "criadas", mais antiga a simulação
                    long diasAtras = (long) (i * 10 + j * 5 + k + 1);
                    OffsetDateTime dataSimulacao = agora.minusDays(diasAtras);

                    createSimulation(customer, product, valor, prazo, dataSimulacao);
                    criadas++;
                }
            }
        }

        long total = simulationRepository.count();
        LOG.infof("DatabaseSeeder: criadas %d simulações de teste (antes: %d, agora: %d).",
                criadas, inicial, total);
    }

    private void createSimulation(Customer customer,
                                  InvestmentProduct product,
                                  double valor,
                                  int prazoMeses,
                                  OffsetDateTime dataSimulacao) {

        // cálculo simples apenas para efeito de exemplo
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
}
