package uk.gov.pay.directdebit.junit;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.OptionalContainerFactory;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

public class TestContext {

    private DBI jdbi;
    // This should be out of the text context really (since it is a specific class for this project)
    // but is fine for now
    private DatabaseTestHelper databaseTestHelper;
    private int port;

    public TestContext(int port, DataSourceFactory dataSourceFactory) {
        jdbi = new DBI(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
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
}
