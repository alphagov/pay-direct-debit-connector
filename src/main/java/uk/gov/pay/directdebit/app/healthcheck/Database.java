package uk.gov.pay.directdebit.app.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DriverManager;

public class Database extends HealthCheck {

    private DirectDebitConfig configuration;

    @Inject
    public Database(DirectDebitConfig configuration) {
        this.configuration = configuration;
    }

    @Override
    protected Result check() throws Exception {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                configuration.getDataSourceFactory().getUrl(),
                configuration.getDataSourceFactory().getUser(),
                configuration.getDataSourceFactory().getPassword());
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
