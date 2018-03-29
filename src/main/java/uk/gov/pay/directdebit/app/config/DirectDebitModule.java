package uk.gov.pay.directdebit.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gocardless.GoCardlessClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.squareup.okhttp.OkHttpClient;
import io.dropwizard.setup.Environment;
import org.apache.commons.lang3.reflect.FieldUtils;
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
import uk.gov.service.notify.NotificationClient;

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

    // Nasty hack alert - gocardless client does not seem to pick up the certificates in our trust store automatically, so we need to inject those. The underlying client (okhttp) supports that, but it's not accessible. So we make it accessible *** godmode ***. Sent an email to gocardless about the issue.
    //fixme there's an ongoing conversation with gocardless to avoid having to do this
    private GoCardlessClient hackedGoCardlessClient(DirectDebitConfig config, SSLSocketFactory sslSocketFactory) throws IllegalAccessException {
        GoCardlessClient goCardlessClient = configuration.getGoCardless().buildClient();
        Object httpClient = FieldUtils.readField(goCardlessClient, "httpClient", true);
        OkHttpClient rawClient = (OkHttpClient) FieldUtils.readField(httpClient, "rawClient", true);
        if (configuration.getGoCardless().isCallingStubs()) {
            rawClient.setSslSocketFactory(sslSocketFactory);
        } else {
            rawClient.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getProxyConfig().getHost(), config.getProxyConfig().getPort())));
        }
        return goCardlessClient;
    }

    @Provides
    public GoCardlessClientWrapper provideGoCardlessClientWrapper() throws IllegalAccessException {
        GoCardlessClient goCardlessClient =  hackedGoCardlessClient(configuration, sslSocketFactory);
        return new GoCardlessClientWrapper(goCardlessClient);
    }

    @Provides
    public NotificationClient provideNotifyClient() {
        return configuration.getNotifyConfig().getInstance();
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
