package uk.gov.pay.directdebit.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gocardless.GoCardlessClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientWrapper;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;
import uk.gov.pay.directdebit.webhook.gocardless.support.WebhookVerifier;

import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class DirectDebitModule extends AbstractModule {

    private final DirectDebitConfig configuration;
    private final Environment environment;
    private final DBI dbi;
    private final SSLSocketFactory sslSocketFactory;

    public DirectDebitModule(final DirectDebitConfig configuration, final Environment environment, final DBI dbi, final SSLSocketFactory socketFactory) {
        this.configuration = configuration;
        this.environment = environment;
        this.dbi = dbi;
        this.sslSocketFactory = socketFactory;
    }

    @Override
    protected void configure() {
        bind(DirectDebitConfig.class).toInstance(configuration);
        bind(Environment.class).toInstance(environment);
    }

    @Provides
    public GoCardlessClientWrapper provideGoCardlessClientWrapper() throws IllegalAccessException {
        GoCardlessClient.Builder builder = GoCardlessClient.newBuilder(configuration.getGoCardless().getAccessToken())
                .withEnvironment(configuration.getGoCardless().getEnvironment());
        if (configuration.getGoCardless().isCallingStubs()) {
            builder.withSslSocketFactory(sslSocketFactory)
                    .withBaseUrl(configuration.getGoCardless().getClientUrl());
        } else {
            builder.withProxy(new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress(configuration.getProxyConfig().getHost(), configuration.getProxyConfig().getPort())));
        }
        return new GoCardlessClientWrapper(builder.build());
    }


    @Provides
    public WebhookVerifier provideWebhookVerifier() {
        return configuration.getGoCardless().buildSignatureVerifier();
    }

    @Provides
    public ObjectMapper provideObjectMapper() {
        return environment.getObjectMapper();
    }

    @Provides
    public TransactionDao provideTransactionDao() {
        return dbi.onDemand(TransactionDao.class);
    }
    @Provides
    public PaymentRequestEventDao providePaymentRequestEventDao() {
        return dbi.onDemand(PaymentRequestEventDao.class);
    }
    @Provides
    public TokenDao provideTokenDao() {
        return dbi.onDemand(TokenDao.class);
    }
    @Provides
    public PaymentRequestDao providePaymentRequestDao() {
        return dbi.onDemand(PaymentRequestDao.class);
    }
    @Provides
    public GatewayAccountDao provideGatewayAccountDao() {
        return dbi.onDemand(GatewayAccountDao.class);
    }
    @Provides
    public PayerDao providePayerDao() {
        return dbi.onDemand(PayerDao.class);
    }
    @Provides
    public MandateDao provideMandateDao() {
        return dbi.onDemand(MandateDao.class);
    }
    @Provides
    public GoCardlessCustomerDao provideGoCardlessCustomerDao() {
        return dbi.onDemand(GoCardlessCustomerDao.class);
    }
    @Provides
    public GoCardlessPaymentDao provideGoCardlessPaymentDao() {
        return dbi.onDemand(GoCardlessPaymentDao.class);
    }
    @Provides
    public GoCardlessMandateDao provideGoCardlessMandateDao() {
        return dbi.onDemand(GoCardlessMandateDao.class);
    }
    @Provides
    public GoCardlessEventDao provideGoCardlessEventDao() {
        return dbi.onDemand(GoCardlessEventDao.class);
    }
}
