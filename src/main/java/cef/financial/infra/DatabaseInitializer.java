package cef.financial.infra;

import io.agroal.api.AgroalDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@ApplicationScoped
public class DatabaseInitializer {

    private static final Logger LOG = Logger.getLogger(DatabaseInitializer.class);

    @Inject
    AgroalDataSource dataSource;   // usa o <default>, não tem @Named

    @PostConstruct
    void init() {
        LOG.info("Verificando estrutura do banco investments (tabelas principais).");

        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {

            ensureInvestmentProductTable(st);
            ensureInvestmentHistoryTable(st);
            ensureInvestmentSimulationTable(st);

        } catch (Exception e) {
            LOG.error("Erro ao inicializar/garantir as tabelas do banco investments", e);
        }
    }

    private void ensureInvestmentProductTable(Statement st) throws Exception {
        boolean exists = false;
        try (ResultSet rs = st.executeQuery("""
                SELECT 1
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_NAME = 'HTE_INVESTMENT_PRODUCT'
                """)) {
            if (rs.next()) {
                exists = true;
            }
        }

        if (exists) {
            LOG.info("Tabela HTE_INVESTMENT_PRODUCT já existe. Nenhuma ação necessária.");
            return;
        }

        LOG.warn("Tabela HTE_INVESTMENT_PRODUCT NÃO encontrada. Criando tabela...");

        String createTableSql = """
                CREATE TABLE HTE_INVESTMENT_PRODUCT (
                    liquidez_dias        INT             NULL,
                    prazo_max_meses      INT             NULL,
                    prazo_min_meses      INT             NULL,
                    rn_                  INT             NOT NULL,
                    id                   BIGINT          NULL,
                    rentabilidade_anual  FLOAT           NULL,
                    hib_sess_id          CHAR(36)        NOT NULL,
                    nome                 VARCHAR(255)    NULL,
                    perfil_recomendado   VARCHAR(255)    NULL,
                    risco                VARCHAR(255)    NULL,
                    tipo                 VARCHAR(255)    NULL,
                    CONSTRAINT PK_HTE_INVESTMENT_PRODUCT
                        PRIMARY KEY (rn_, hib_sess_id)
                );
                """;

        st.executeUpdate(createTableSql);
        LOG.info("Tabela HTE_INVESTMENT_PRODUCT criada com sucesso.");
    }

    private void ensureInvestmentHistoryTable(Statement st) throws Exception {
        boolean exists = false;
        try (ResultSet rs = st.executeQuery("""
                SELECT 1
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_NAME = 'HTE_INVESTMENT_HISTORY'
                """)) {
            if (rs.next()) {
                exists = true;
            }
        }

        if (exists) {
            LOG.info("Tabela HTE_INVESTMENT_HISTORY já existe. Nenhuma ação necessária.");
            return;
        }

        LOG.warn("Tabela HTE_INVESTMENT_HISTORY NÃO encontrada. Criando tabela...");

        String createTableSql = """
                CREATE TABLE HTE_INVESTMENT_HISTORY (
                    id                 BIGINT IDENTITY(1,1) PRIMARY KEY,
                    CLIENTE_ID         BIGINT        NOT NULL,
                    TIPO               VARCHAR(100)  NULL,
                    VALOR              FLOAT         NOT NULL,
                    RENTABILIDADE      FLOAT         NOT NULL,
                    DATA_INVESTIMENTO  DATE          NULL
                );
                """;

        st.executeUpdate(createTableSql);
        LOG.info("Tabela HTE_INVESTMENT_HISTORY criada com sucesso.");
    }

    private void ensureInvestmentSimulationTable(Statement st) throws Exception {
        boolean exists = false;
        try (ResultSet rs = st.executeQuery("""
                SELECT 1
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_NAME = 'HTE_INVESTMENT_SIMULATION'
                """)) {
            if (rs.next()) {
                exists = true;
            }
        }

        if (exists) {
            LOG.info("Tabela HTE_INVESTMENT_SIMULATION já existe. Nenhuma ação necessária.");
            return;
        }

        LOG.warn("Tabela HTE_INVESTMENT_SIMULATION NÃO encontrada. Criando tabela...");

        String createTableSql = """
                CREATE TABLE HTE_INVESTMENT_SIMULATION (
                    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
                    clienteId       BIGINT         NOT NULL,
                    produto_id      BIGINT         NOT NULL,
                    valorInvestido  FLOAT          NOT NULL,
                    valorFinal      FLOAT          NOT NULL,
                    prazoMeses      INT            NOT NULL,
                    dataSimulacao   DATETIME2      NOT NULL
                );
                """;

        st.executeUpdate(createTableSql);
        LOG.info("Tabela HTE_INVESTMENT_SIMULATION criada com sucesso.");
    }
}
