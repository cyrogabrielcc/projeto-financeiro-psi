package cef.financial;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@QuarkusMain
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class);

    public static void main(String... args) {
        // 1) Tenta garantir que o banco exista
        ensureDatabaseExists();

        // 2) Sobe o Quarkus normalmente
        Quarkus.run(args);
    }

    private static void ensureDatabaseExists() {
        String host = "localhost";
        String port = "1433";
        String adminUser = "sa";                      // mesmo user que você usa no application.properties
        String adminPassword = "YourStrong!Passw0rd"; // MESMA senha do application.properties
        String dbName = "investments";

        // Importante: usa databaseName=master (igual estilo da sua URL)
        String url = "jdbc:sqlserver://" + host + ":" + port
                + ";databaseName=master"
                + ";encrypt=false;trustServerCertificate=true";

        try {
            // Carrega o driver do SQL Server (se já tiver no pom.xml)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            LOG.infov("Conectando em {0}:{1} no banco master para garantir o DB {2}",
                    host, port, dbName);

            try (Connection conn = DriverManager.getConnection(url, adminUser, adminPassword);
                 Statement st = conn.createStatement()) {

                String sql = """
                        IF DB_ID('%s') IS NULL
                        BEGIN
                            PRINT 'Criando banco de dados %s';
                            CREATE DATABASE %s;
                        END
                        """.formatted(dbName, dbName, dbName);

                st.executeUpdate(sql);
                LOG.infov("Banco de dados {0} garantido (existente ou recém-criado).", dbName);
            }

        } catch (Exception e) {
            // ⚠️ IMPORTANTE: NÃO MATAR A APLICAÇÃO
            LOG.error("Não foi possível garantir a existência do banco investments. " +
                    "Verifique se o SQL Server está rodando e se o usuário/senha estão corretos.", e);
            // Se falhar aqui, o Quarkus ainda vai tentar subir.
            // Se o DB de fato não existir, aí o erro vai aparecer no datasource depois.
        }
    }
}
