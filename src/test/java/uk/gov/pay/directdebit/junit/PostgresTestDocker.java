package uk.gov.pay.directdebit.junit;

import com.spotify.docker.client.DefaultDockerClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static java.sql.DriverManager.getConnection;
import static org.junit.Assert.assertNotNull;
import static uk.gov.pay.directdebit.junit.PostgresTestContainer.DB_PASSWORD;
import static uk.gov.pay.directdebit.junit.PostgresTestContainer.DB_USERNAME;

final class PostgresTestDocker {

    static final String DB_NAME = "directdebitconnectortests";
    private static final String DOCKER_HOST = "DOCKER_HOST";
    private static final String DOCKER_CERT_PATH = "DOCKER_CERT_PATH";
    private static final String HOST = dockerHostLocalAware();
    private static PostgresTestContainer container;

    static void getOrCreate(String image) {
        try {
            if (container == null) {
                container = new PostgresTestContainer(DefaultDockerClient.fromEnv().build(), HOST, image);
                createDatabase();
            }
        } catch (Exception e) {
            throw new PostgresTestDockerException(e);
        }
    }

    private static URI getDockerHostUri() throws URISyntaxException {
        final String dockerHost = Optional.ofNullable(System.getenv(DOCKER_HOST)).
                orElseThrow(() -> new PostgresTestDockerException(DOCKER_HOST + " environment variable not set. It has to be set to the docker daemon location."));
        return new URI(dockerHost);
    }

    private static String dockerHostLocalAware() {
        try {
            URI dockerHostURI = getDockerHostUri();
            final boolean isDockerDaemonLocal = "unix".equals(dockerHostURI.getScheme());
            if (isDockerDaemonLocal) {
                return "localhost";
            } else {
                assertNotNull(DOCKER_CERT_PATH + " environment variable not set.", System.getenv(DOCKER_CERT_PATH));
                return dockerHostURI.getHost();
            }
        } catch (URISyntaxException e) {
            throw new PostgresTestDockerException(e);
        }
    }

    private static void createDatabase() {
        try (Connection connection = getConnection(container.getPostgresDbUri(), DB_USERNAME, DB_PASSWORD)) {
            connection.createStatement().execute("CREATE DATABASE " + DB_NAME + " WITH owner=" + DB_USERNAME + " TEMPLATE postgres");
            connection.createStatement().execute("GRANT ALL PRIVILEGES ON DATABASE " + DB_NAME + " TO " + DB_USERNAME);
        } catch (SQLException e) {
            throw new PostgresTestDockerException(e);
        }
    }

    static String getDbRootUri() {
        return container.getPostgresDbUri();
    }

    static String getDbUri() {
        return getDbRootUri() + DB_NAME;
    }
}
