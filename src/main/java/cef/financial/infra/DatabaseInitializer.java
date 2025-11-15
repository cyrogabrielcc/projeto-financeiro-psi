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
        LOG.info("Verificando estrutura do banco investments (tabelas).");

        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {

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
                        liquidez_dias        INT,
                        prazo_max_meses      INT,
                        prazo_min_meses      INT,
                        rn_                  INT          NOT NULL,
                        id                   BIGINT,
                        rentabilidade_anual  FLOAT,
                        hib_sess_id          CHAR(36)     NOT NULL,
                        nome                 VARCHAR(255),
                        perfil_recomendado   VARCHAR(255),
                        risco                VARCHAR(255),
                        tipo                 VARCHAR(255),
                        CONSTRAINT PK_HTE_INVESTMENT_PRODUCT
                            PRIMARY KEY (rn_, hib_sess_id)
                    );
                    """;

            st.executeUpdate(createTableSql);
            LOG.info("Tabela HTE_INVESTMENT_PRODUCT criada com sucesso.");

        } catch (Exception e) {
            LOG.error("Erro ao inicializar/garantir a tabela HTE_INVESTMENT_PRODUCT", e);
        }
    }
}
