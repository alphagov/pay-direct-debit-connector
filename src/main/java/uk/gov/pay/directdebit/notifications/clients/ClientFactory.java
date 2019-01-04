package uk.gov.pay.directdebit.notifications.clients;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientProperties;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.filters.LoggingFilter;
import uk.gov.pay.directdebit.app.ssl.TrustStoreLoader;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.ws.rs.client.Client;

public class ClientFactory {

    private final Environment environment;
    private final DirectDebitConfig conf;

    @Inject
    public ClientFactory(Environment environment, DirectDebitConfig conf) {
        this.environment = environment;
        this.conf = conf;
    }

    public Client createWithDropwizardClient(String clientName) {
        JerseyClientConfiguration clientConfiguration = conf.getClientConfiguration();
        JerseyClientBuilder defaultClientBuilder = new JerseyClientBuilder(environment)
                .using(new ApacheConnectorProvider())
                .using(clientConfiguration)
                .withProperty(ClientProperties.READ_TIMEOUT, (int) conf.getCustomJerseyClient().getReadTimeout().toMilliseconds())
                .withProperty(ApacheClientProperties.CONNECTION_MANAGER, createConnectionManager());

        Client client = defaultClientBuilder.build(clientName);
        client.register(LoggingFilter.class);
        return client;
    }

    private HttpClientConnectionManager createConnectionManager() {
        return new PoolingHttpClientConnectionManager(
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https",
                                new SSLConnectionSocketFactory(
                                        SslConfigurator
                                                .newInstance()
                                                .trustStore(TrustStoreLoader.getTrustStore())
                                                .createSSLContext(),
                                        new String[]{"TLSv1.2"},
                                        null,
                                        (HostnameVerifier) null
                                )
                        )
                        .build(),
                new ManagedHttpClientConnectionFactory()
        );
    }
}

