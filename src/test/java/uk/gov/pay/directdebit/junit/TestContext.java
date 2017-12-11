package uk.gov.pay.directdebit.junit;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.OptionalContainerFactory;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

public class TestContext {

    private final String databaseUrl;
    private DBI jdbi;
    // This should be out of the text context really (since it is a specific class for this project)
    // but is fine for now
    private DatabaseTestHelper databaseTestHelper;
    private int port;
    private final String databaseUser;
    private final String databasePassword;

    public TestContext(int port, DataSourceFactory dataSourceFactory) {
        databaseUrl = dataSourceFactory.getUrl();
        databaseUser = dataSourceFactory.getUser();
        databasePassword = dataSourceFactory.getPassword();
        jdbi = new DBI(databaseUrl, databaseUser, databasePassword);
        jdbi.registerContainerFactory(new OptionalContainerFactory());
        this.databaseTestHelper = new DatabaseTestHelper(jdbi);
        this.port = port;
    }

    public DBI getJdbi() {
        return jdbi;
    }

    public DatabaseTestHelper getDatabaseTestHelper() {
        return databaseTestHelper;
    }

    public int getPort() {
        return port;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }
}
