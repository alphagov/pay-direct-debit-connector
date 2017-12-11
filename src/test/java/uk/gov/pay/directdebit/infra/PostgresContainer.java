package uk.gov.pay.directdebit.infra;

import com.google.common.base.Stopwatch;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.LogConfig;
import com.spotify.docker.client.messages.PortBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;
import static java.sql.DriverManager.getConnection;
import static java.util.stream.Collectors.joining;

class PostgresContainer {

    static final String DB_USERNAME = "postgres";
    static final String DB_PASSWORD = "mysecretpassword";

    private static final Logger LOG = LoggerFactory.getLogger(PostgresContainer.class);
    private static final String GOVUK_POSTGRES_IMAGE = "govukpay/postgres:9.4.4";
    private static final String INTERNAL_PORT = "5432";
    private static final int DB_TIMEOUT_SEC = 15;

    private final String containerId;
    private final DockerClient docker;
    private final String postgresUri;

    private volatile boolean stopped = false;

    PostgresContainer(DockerClient docker, String host) throws Exception {
        Class.forName("org.postgresql.Driver");
        failsafeDockerPull(docker, GOVUK_POSTGRES_IMAGE);
        this.docker = docker;
        this.containerId = createContainer(docker);
        docker.startContainer(containerId);
        this.postgresUri = "jdbc:postgresql://" + host + ":" + getContainerPortBy(docker, containerId) + "/";
        registerShutdownHook();
        waitForPostgresToStart();
    }

    String getPostgresDbUri() {
        return postgresUri;
    }

    void stop() {
        if (!stopped) {
            try {
                stopped = true;
                LOG.info("Killing postgres container with ID: " + containerId);
                LogStream logs = docker.logs(containerId, DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr());
                LOG.info("Killed container logs:\n");
                logs.attach(System.out, System.err);
                docker.stopContainer(containerId, 5);
                docker.removeContainer(containerId);
            } catch (Exception e) {
                LOG.error("Could not shutdown " + containerId, e);
            }
        }
    }

    private String createContainer(DockerClient docker) throws DockerException, InterruptedException {
        docker.listImages(DockerClient.ListImagesParam.create("name", GOVUK_POSTGRES_IMAGE));
        final HostConfig hostConfig = HostConfig.builder().logConfig(LogConfig.create("json-file")).publishAllPorts(true).build();
        ContainerConfig containerConfig = ContainerConfig.builder()
                .image(GOVUK_POSTGRES_IMAGE)
                .hostConfig(hostConfig)
                .env("POSTGRES_USER=" + DB_USERNAME, "POSTGRES_PASSWORD=" + DB_PASSWORD)
                .build();
        return docker.createContainer(containerConfig).id();
    }

    private int getContainerPortBy(DockerClient docker, String containerId) throws Exception {
        ContainerInfo containerInfo = docker.inspectContainer(containerId);
        List<PortBinding> portBindings = containerInfo.networkSettings().ports().get(INTERNAL_PORT + "/tcp");
        LOG.info("Postgres host port: {}", portBindings.stream().map(PortBinding::hostPort).collect(joining(", ")));
        return parseInt(portBindings.get(0).hostPort());
    }

    private void failsafeDockerPull(DockerClient docker, String image) {
        try {
            docker.pull(image);
        } catch (Exception e) {
            LOG.error("Docker image " + image + " could not be pulled from DockerHub", e);
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private void waitForPostgresToStart() throws DockerException, InterruptedException, IOException {
        Stopwatch timer = Stopwatch.createStarted();
        boolean succeeded = false;
        while (!succeeded && timer.elapsed(TimeUnit.SECONDS) < DB_TIMEOUT_SEC) {
            Thread.sleep(500);
            succeeded = checkPostgresConnection();
        }
        if (!succeeded) {
            throw new RuntimeException("Postgres did not start in " + DB_TIMEOUT_SEC + " seconds.");
        }
        LOG.info("Postgres docker container started in {}.", timer.elapsed(TimeUnit.MILLISECONDS));
    }

    private boolean checkPostgresConnection() throws IOException {
        try (Connection connection = getConnection(getPostgresDbUri(), DB_USERNAME, DB_PASSWORD)) {
            return true;
        } catch (Exception except) {
            return false;
        }
    }
}
