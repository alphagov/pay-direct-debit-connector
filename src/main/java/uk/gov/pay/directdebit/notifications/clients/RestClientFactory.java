package uk.gov.pay.api.app;

import org.glassfish.jersey.SslConfigurator;
import uk.gov.pay.directdebit.app.filters.LoggingFilter;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import static uk.gov.pay.directdebit.app.ssl.TrustStoreLoader.getTrustStore;
import static uk.gov.pay.directdebit.app.ssl.TrustStoreLoader.getTrustStorePassword;

public class RestClientFactory {
    public static final String TLSV1_2 = "TLSv1.2";

    public static Client buildClient() {
        SslConfigurator sslConfig = SslConfigurator.newInstance()
                .trustStore(getTrustStore())
                .trustStorePassword(getTrustStorePassword())
                .securityProtocol(TLSV1_2);

        SSLContext sslContext = sslConfig.createSSLContext();
        Client client = ClientBuilder.newBuilder().sslContext(sslContext).build();
        client.register(LoggingFilter.class);
        return client;
    }

    private RestClientFactory() {
    }
}
