package uk.gov.pay.directdebit.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gocardless.GoCardlessClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.notifications.clients.AdminUsersClient;
import uk.gov.pay.directdebit.notifications.clients.RestClientFactory;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientWrapper;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;
import uk.gov.pay.directdebit.webhook.gocardless.config.GoCardlessFactory;
import uk.gov.pay.directdebit.webhook.gocardless.support.WebhookVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class DirectDebitModule extends AbstractModule {

    private final DirectDebitConfig configuration;
    private final Environment environment;
    private final Jdbi jdbi;
    private final SSLSocketFactory sslSocketFactory;

    public DirectDebitModule(final DirectDebitConfig configuration, final Environment environment, final Jdbi jdbi, final SSLSocketFactory socketFactory) {
        this.configuration = configuration;
        this.environment = environment;
        this.jdbi = jdbi;
        this.sslSocketFactory = socketFactory;
    }

    @Override
    protected void configure() {
        bind(DirectDebitConfig.class).toInstance(configuration);
        bind(Environment.class).toInstance(environment);
        AdminUsersConfig config = configuration.getAdminUsersConfig();
        AdminUsersClient adminUsersClient = new AdminUsersClient(config, RestClientFactory.buildClient());
        UserNotificationService userNotificationService = new UserNotificationService(adminUsersClient, configuration);
        bind(UserNotificationService.class).toInstance(userNotificationService);
    }

    private GoCardlessClient createGoCardlessClient() {
        GoCardlessFactory goCardlessFactory = configuration.getGoCardless();
        GoCardlessClient.Builder builder = GoCardlessClient.newBuilder(
                goCardlessFactory.getAccessToken());

        if (goCardlessFactory.isCallingStubs()) {
            return builder.withBaseUrl(goCardlessFactory.getClientUrl())
                    .withSslSocketFactory(sslSocketFactory)
                    .build();
        }
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(configuration.getProxyConfig().getHost(), configuration.getProxyConfig().getPort()));
        return builder.withEnvironment(goCardlessFactory.getEnvironment())
                .withProxy(proxy).build();
    }

    @Provides
    @Singleton
    public GoCardlessClientWrapper provideGoCardlessClientWrapper()  {
        return new GoCardlessClientWrapper(createGoCardlessClient());
    }

    @Provides
    @Singleton
    public WebhookVerifier provideWebhookVerifier() {
        return configuration.getGoCardless().buildSignatureVerifier();
    }

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        return environment.getObjectMapper();
    }

    @Provides
    @Singleton
    public TransactionDao provideTransactionDao() {
        return jdbi.onDemand(TransactionDao.class);
    }

    @Provides
    @Singleton
    public PaymentRequestEventDao providePaymentRequestEventDao() {
        return jdbi.onDemand(PaymentRequestEventDao.class);
    }

    @Provides
    @Singleton
    public TokenDao provideTokenDao() {
        return jdbi.onDemand(TokenDao.class);
    }

    @Provides
    @Singleton
    public PaymentRequestDao providePaymentRequestDao() {
        return jdbi.onDemand(PaymentRequestDao.class);
    }

    @Provides
    @Singleton
    public GatewayAccountDao provideGatewayAccountDao() {
        return jdbi.onDemand(GatewayAccountDao.class);
    }

    @Provides
    @Singleton
    public PayerDao providePayerDao() {
        return jdbi.onDemand(PayerDao.class);
    }

    @Provides
    @Singleton
    public MandateDao provideMandateDao() {
        return jdbi.onDemand(MandateDao.class);
    }

    @Provides
    @Singleton
    public GoCardlessCustomerDao provideGoCardlessCustomerDao() {
        return jdbi.onDemand(GoCardlessCustomerDao.class);
    }

    @Provides
    @Singleton
    public GoCardlessPaymentDao provideGoCardlessPaymentDao() {
        return jdbi.onDemand(GoCardlessPaymentDao.class);
    }

    @Provides
    @Singleton
    public GoCardlessMandateDao provideGoCardlessMandateDao() {
        return jdbi.onDemand(GoCardlessMandateDao.class);
    }

    @Provides
    @Singleton
    public GoCardlessEventDao provideGoCardlessEventDao() {
        return jdbi.onDemand(GoCardlessEventDao.class);
    }

}
