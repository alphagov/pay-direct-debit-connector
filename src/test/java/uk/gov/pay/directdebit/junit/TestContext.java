package uk.gov.pay.directdebit.junit;

import io.dropwizard.db.DataSourceFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

public class TestContext {

    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;
    private Jdbi jdbi;
    // This should be out of the text context really (since it is a specific class for this project)
    // but is fine for now
    private DatabaseTestHelper databaseTestHelper;
    private int port;

    public TestContext(int port, DataSourceFactory dataSourceFactory) {
        databaseUrl = dataSourceFactory.getUrl();
        databaseUser = dataSourceFactory.getUser();
        databasePassword = dataSourceFactory.getPassword();
        jdbi = Jdbi.create(databaseUrl, databaseUser, databasePassword);
        jdbi.installPlugin(new SqlObjectPlugin());
        this.databaseTestHelper = new DatabaseTestHelper(jdbi);
        this.port = port;
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    public DatabaseTestHelper getDatabaseTestHelper() {
        return databaseTestHelper;
    }

    public int getPort() {
        return port;
    }

    String getDatabaseUrl() {
        return databaseUrl;
    }

    String getDatabaseUser() {
        return databaseUser;
    }

    String getDatabasePassword() {
        return databasePassword;
    }
}
