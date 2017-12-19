package uk.gov.pay.directdebit.app.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DatabaseResourceWaitCommand {

    private static final int PROGRESSIVE_SECONDS_TO_WAIT = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseResourceWaitCommand.class);

    private final DirectDebitConfig configuration;

    DatabaseResourceWaitCommand(DirectDebitConfig configuration) {
        this.configuration = configuration;
    }

    void doWait() {
        LOGGER.info("Checking for database availability >>>");
        boolean databaseAvailable = isDatabaseAvailable();

        long timeToWait = 0;
        while(!databaseAvailable) {
            timeToWait += PROGRESSIVE_SECONDS_TO_WAIT;
            LOGGER.info("Waiting for "+ timeToWait +" seconds till the database is available ...");
            sleep(timeToWait * 1000);
            databaseAvailable = isDatabaseAvailable();
        }
        LOGGER.info("Database available.");
    }


    private boolean isDatabaseAvailable() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    configuration.getDataSourceFactory().getUrl(),
                    configuration.getDataSourceFactory().getUser(),
                    configuration.getDataSourceFactory().getPassword());
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            if(connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("Error closing connection acquired for Database check");
                }
            }
        }
    }

    private void sleep(long durationSeconds) {
        try {
            Thread.sleep(durationSeconds);
        } catch (InterruptedException ignored) {
        }
    }
}
