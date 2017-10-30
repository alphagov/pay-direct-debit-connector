package uk.gov.pay.directdebit.app.healthchecks;

import uk.gov.pay.directdebit.app.config.DirectDebitConfig;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;

public class ApplicationStartupDependentResource {

    private final DirectDebitConfig configuration;

    @Inject
    public ApplicationStartupDependentResource(DirectDebitConfig configuration) {
        this.configuration = configuration;
    }

    public Connection getDatabaseConnection() throws SQLException {
        //TODO: Awaiting AWS DB environment ready
//        return DriverManager.getConnection(
//                configuration.getDataSourceFactory().getUrl(),
//                configuration.getDataSourceFactory().getUser(),
//                configuration.getDataSourceFactory().getPassword());
        return null;
    }

    public void sleep(long durationSeconds) {
        try {
            Thread.sleep(durationSeconds);
        } catch (InterruptedException ignored) {}
    }

}
