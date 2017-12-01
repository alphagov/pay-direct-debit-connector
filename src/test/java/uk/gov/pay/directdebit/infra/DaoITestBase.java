package uk.gov.pay.directdebit.infra;

import io.dropwizard.jdbi.OptionalContainerFactory;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DaoITestBase {
    @Rule
    public PostgresDockerRule postgres;

    protected DBI jdbi;
    protected DatabaseTestHelper databaseTestHelper;

    public DaoITestBase() {
        postgres = new PostgresDockerRule();
        jdbi = new DBI(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword());
        jdbi.registerContainerFactory(new OptionalContainerFactory());
    }

    @Before
    public void setupTests() throws Exception {
        final Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.driver", postgres.getDriverClass());
        properties.put("javax.persistence.jdbc.url", postgres.getConnectionUrl());
        properties.put("javax.persistence.jdbc.user", postgres.getUsername());
        properties.put("javax.persistence.jdbc.password", postgres.getPassword());

        databaseTestHelper = new DatabaseTestHelper(jdbi);

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword());

            Liquibase migrator = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
            migrator.update("");
        } finally {
            if(connection != null)
                connection.close();
        }

    }

    @After
    public void tearDown() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword());
            Liquibase migrator = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
            migrator.dropAll();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
