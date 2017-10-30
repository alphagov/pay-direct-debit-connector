package uk.gov.pay.directdebit.app.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;

import javax.inject.Inject;
import java.sql.Connection;

public class DatabaseHealthCheck extends HealthCheck {

    private DirectDebitConfig configuration;

    @Inject
    public DatabaseHealthCheck(DirectDebitConfig configuration) {
        this.configuration = configuration;
    }

    @Override
    protected Result check() throws Exception {
        Connection connection = null;
        try {
            //TODO: Awaiting AWS DB environment ready
//            connection = DriverManager.getConnection(
//                configuration.getDataSourceFactory().getUrl(),
//                configuration.getDataSourceFactory().getUser(),
//                configuration.getDataSourceFactory().getPassword());
            connection.setReadOnly(true);
            return connection.isValid(2) ? Result.healthy() : Result.unhealthy("Could not validate the DB connection.");
        } catch (Exception e) {
            return Result.unhealthy(e.getMessage());
        } finally {
            if (connection !=null) {
                connection.close();
            }
        }
    }

}
