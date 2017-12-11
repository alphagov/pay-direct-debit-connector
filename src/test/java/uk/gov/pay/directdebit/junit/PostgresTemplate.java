package uk.gov.pay.directdebit.junit;

import io.dropwizard.db.DataSourceFactory;

import java.sql.Connection;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

public class PostgresTemplate {

    private static final String TEMPLATE_NAME = "templateddc";

    public static void createTemplate(DataSourceFactory source) {
        PostgresConfig config = PostgresConfig.valueOf(source);
        try (Connection connection = getConnection(config.getDatabaseRootUri(), config.getUserName(), config.getPassword())) {
            terminateDbConnections(connection, config.getDatabaseName());
            connection.createStatement().execute("CREATE DATABASE " + TEMPLATE_NAME + " WITH TEMPLATE " + config.getDatabaseName() + " OWNER " + config.getUserName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void restoreTemplate(DataSourceFactory source) {
        PostgresConfig config = PostgresConfig.valueOf(source);
        try (Connection connection = getConnection(config.getDatabaseRootUri(), config.getUserName(), config.getPassword())) {
            terminateDbConnections(connection, config.getDatabaseName());
            connection.createStatement().execute("DROP DATABASE " + config.getDatabaseName());
            connection.createStatement().execute("CREATE DATABASE " + config.getDatabaseName() + " WITH TEMPLATE " + TEMPLATE_NAME + " OWNER " + config.getUserName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void terminateDbConnections(Connection connection, String databaseName) throws SQLException {
        connection.createStatement().execute("SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity"
                + " WHERE pg_stat_activity.datname = '" + databaseName + "' AND pid <> pg_backend_pid()");
    }

    private static class PostgresConfig {

        private final String username;
        private final String password;
        private final String databaseName;
        private final String databaseRootUri;

        private PostgresConfig(String username, String password, String databaseName, String databaseRootUri) {
            this.username = username;
            this.password = password;
            this.databaseName = databaseName;
            this.databaseRootUri = databaseRootUri;
        }

        private static final PostgresConfig valueOf(DataSourceFactory datasource) {
            String databaseUri = datasource.getUrl();
            int indexDbName = databaseUri.lastIndexOf("/");
            return new PostgresConfig(datasource.getUser(), datasource.getPassword(), databaseUri.substring(indexDbName + 1), databaseUri.substring(0, indexDbName + 1));
        }

        String getUserName() {
            return username;
        }

        String getPassword() {
            return password;
        }

        String getDatabaseName() {
            return databaseName;
        }

        String getDatabaseRootUri() {
            return databaseRootUri;
        }
    }
}
