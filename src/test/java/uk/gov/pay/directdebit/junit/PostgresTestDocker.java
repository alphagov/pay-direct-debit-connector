package uk.gov.pay.directdebit.junit;

import uk.gov.service.payments.commons.testing.db.PostgresContainer;

import java.sql.Connection;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

final class PostgresTestDocker {

    private static final String DB_NAME = "directdebit_connector_tests";
    private static PostgresContainer container;

    static void getOrCreate() {
        try {
            if (container == null) {
                container = new PostgresContainer();
                createDatabase(DB_NAME);
            }
        } catch (Exception e) {
            throw new PostgresTestDockerException(e);
        }
    }
    
    private static void createDatabase(final String dbName) {
        final String dbUser = getDbUsername();

        try (Connection connection = getConnection(getDbRootUri(), dbUser, getDbPassword())) {
            connection.createStatement().execute("CREATE DATABASE " + dbName + " WITH owner=" + dbUser + " TEMPLATE postgres");
            connection.createStatement().execute("GRANT ALL PRIVILEGES ON DATABASE " + dbName + " TO " + dbUser);
        } catch (SQLException e) {
            throw new PostgresTestDockerException(e);
        }
    }

    private static String getDbRootUri() {
        return container.getConnectionUrl();
    }

    static String getDbUri() {
        return getDbRootUri() + DB_NAME;
    }

    static String getDbPassword() {
        return container.getPassword();
    }

    static String getDbUsername() {
        return container.getUsername();
    }
}
