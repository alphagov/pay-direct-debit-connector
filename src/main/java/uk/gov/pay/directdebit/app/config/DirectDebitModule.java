package uk.gov.pay.directdebit.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.setup.Environment;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.clients.GoCardlessClientFactory;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.dao.SandboxEventDao;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.dao.MandateSearchDao;
import uk.gov.pay.directdebit.notifications.clients.AdminUsersClient;
import uk.gov.pay.directdebit.notifications.clients.ClientFactory;
import uk.gov.pay.directdebit.partnerapp.client.GoCardlessAppConnectClient;
import uk.gov.pay.directdebit.partnerapp.dao.GoCardlessAppConnectAccountTokenDao;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;
import uk.gov.pay.directdebit.webhook.gocardless.support.GoCardlessWebhookSignatureCalculator;

public class DirectDebitModule extends AbstractModule {

    private final DirectDebitConfig configuration;
    private final Environment environment;
    private final Jdbi jdbi;

    public DirectDebitModule(final DirectDebitConfig configuration, final Environment environment, final Jdbi jdbi) {
        this.configuration = configuration;
        this.environment = environment;
        this.jdbi = jdbi;
    }

    @Override
    protected void configure() {
        bind(DirectDebitConfig.class).toInstance(configuration);
        bind(Environment.class).toInstance(environment);
    }


    @Provides
    @Singleton
    public GoCardlessClientFactory provideGoCardlessClientFactory() {
        return new GoCardlessClientFactory(configuration);
    }

    @Provides
    @Singleton
    public GoCardlessAppConnectClient provideGoCardlessConnectClient(ClientFactory clientFactory) {
        return new GoCardlessAppConnectClient(
                configuration.getGoCardlessAppConnectConfig(),
                clientFactory.createWithDropwizardClient("gocardless-appconnect-client"));
    }

    @Provides
    @Singleton
    public AdminUsersClient provideAdminusersClient(ClientFactory clientFactory) {
        return new AdminUsersClient(
                configuration.getAdminUsersConfig(),
                clientFactory.createWithDropwizardClient("adminusers-client"));
    }

    @Provides
    @Singleton
    public GoCardlessWebhookSignatureCalculator provideGoCardlessWebhookSignatureCalculator() {
        if (StringUtils.isBlank(configuration.getGoCardless().getWebhookSecret())) {
            throw new RuntimeException("GoCardless webhook secret is blank");
        }
        return new GoCardlessWebhookSignatureCalculator(configuration.getGoCardless().getWebhookSecret());
    }

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        return environment.getObjectMapper();
    }

    @Provides
    @Singleton
    public PaymentDao providePaymentDao() {
        return jdbi.onDemand(PaymentDao.class);
    }

    @Provides
    @Singleton
    public TokenDao provideTokenDao() {
        return jdbi.onDemand(TokenDao.class);
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
    public GoCardlessEventDao provideGoCardlessEventDao() {
        return jdbi.onDemand(GoCardlessEventDao.class);
    }

    @Provides
    @Singleton
    public SandboxEventDao provideSandboxEventDao(){
        return jdbi.onDemand(SandboxEventDao.class);
    }

    @Provides
    @Singleton
    public PaymentViewDao providePaymentViewDao() {
        return new PaymentViewDao(jdbi);
    }

    @Provides
    @Singleton
    public GoCardlessAppConnectAccountTokenDao providePartnerAppTokenDao() {
        return jdbi.onDemand(GoCardlessAppConnectAccountTokenDao.class);
    }
    
    @Provides
    @Singleton
    public MandateSearchDao provideMandateSearchDao() {
        return new MandateSearchDao(jdbi);
    }
    
    @Provides
    @Singleton
    public GovUkPayEventDao provideGovUkPayEventDao() { return jdbi.onDemand(GovUkPayEventDao.class); }
}
