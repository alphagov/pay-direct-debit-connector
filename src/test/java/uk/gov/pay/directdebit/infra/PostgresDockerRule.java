package uk.gov.pay.directdebit.infra;

import com.spotify.docker.client.DefaultDockerClient;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static java.sql.DriverManager.getConnection;
import static org.junit.Assert.assertNotNull;
import static uk.gov.pay.directdebit.infra.PostgresContainer.DB_PASSWORD;
import static uk.gov.pay.directdebit.infra.PostgresContainer.DB_USERNAME;

public class PostgresDockerRule implements TestRule {

    static final String DB_NAME = "directdebitconnectortests";
    private static final String DOCKER_HOST = "DOCKER_HOST";
    private static final String DOCKER_CERT_PATH = "DOCKER_CERT_PATH";
    private static final String HOST = dockerHostLocalAware();
    private static PostgresContainer container;

    public PostgresDockerRule() {
        try {
            startPostgresIfNecessary();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static URI getDockerHostUri() throws URISyntaxException {
        final String dockerHost = Optional.ofNullable(System.getenv(DOCKER_HOST)).
                orElseThrow(() -> new RuntimeException(DOCKER_HOST + " environment variable not set. It has to be set to the docker daemon location."));
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        createDatabase();
        return statement;
    }

    private void createDatabase() {
        try (Connection connection = getConnection(container.getPostgresDbUri(), DB_USERNAME, DB_PASSWORD)) {
            connection.createStatement().execute("CREATE DATABASE " + DB_NAME + " WITH owner=" + DB_USERNAME + " TEMPLATE postgres");
            connection.createStatement().execute("GRANT ALL PRIVILEGES ON DATABASE " + DB_NAME + " TO " + DB_USERNAME);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    String getDbRootUri() {
        return container.getPostgresDbUri();
    }


    private void startPostgresIfNecessary() throws Exception {
        if (container == null) {
            container = new PostgresContainer(DefaultDockerClient.fromEnv().build(), HOST);
        }
    }
}
