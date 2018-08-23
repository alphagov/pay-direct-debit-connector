package uk.gov.pay.directdebit.notifications.clients;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.proxy.ProxyConfiguration;
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
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.app.ssl.TrustStoreLoader;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.ws.rs.client.Client;

import static java.lang.String.format;

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

    public Client createWithDropwizardClientAndProxy(String clientName) {
        JerseyClientConfiguration clientConfiguration = conf.getClientConfiguration();

        JerseyClientBuilder defaultClientBuilder = new JerseyClientBuilder(environment)
                .using(new ApacheConnectorProvider())
                .using(clientConfiguration)
                .withProperty(ClientProperties.READ_TIMEOUT, (int) conf.getCustomJerseyClient().getReadTimeout().toMilliseconds())
                .withProperty(ApacheClientProperties.CONNECTION_MANAGER, createConnectionManager())
                .withProperty(ClientProperties.PROXY_URI, proxyUrl(conf.getProxyConfig()));

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

    /**
     * Constructs the proxy URL required by JerseyClient property ClientProperties.PROXY_URI
     * <p>
     * <b>NOTE:</b> The reason for doing this is, Dropwizard jersey client doesn't seem to work as per
     * http://www.dropwizard.io/0.9.2/docs/manual/configuration.html#proxy where just setting the proxy config in
     * client configuration is only needed. But after several test, that doesn't seem to work, but by setting the
     * native jersey proxy config as per this implementation seems to work
     * <p>
     * similar problem discussed in here -> https://groups.google.com/forum/#!topic/dropwizard-user/AbDSYfLB17M
     * </p>
     * </p>
     *
     * @param proxyConfig from config.yml
     * @return proxy server URL
     */
    private String proxyUrl(ProxyConfiguration proxyConfig) {
        final String proxyString = format("%s://%s:%s",
                proxyConfig.getScheme(),
                proxyConfig.getHost(),
                proxyConfig.getPort()
        );

        PayLoggerFactory.getLogger(this.getClass()).info(">>>>>>>>>> proxyString [{}]", proxyString);
        return proxyString;
    }
}

