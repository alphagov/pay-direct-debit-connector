package uk.gov.pay.directdebit;

import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.GraphiteUDP;
import com.gocardless.GoCardlessClient;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.squareup.okhttp.OkHttpClient;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.OptionalContainerFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.app.bootstrap.DependentResourcesWaitCommand;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.DirectDebitModule;
import uk.gov.pay.directdebit.app.config.GraphiteConfig;
import uk.gov.pay.directdebit.app.filters.LoggingFilter;
import uk.gov.pay.directdebit.app.healthcheck.Database;
import uk.gov.pay.directdebit.app.healthcheck.Ping;
import uk.gov.pay.directdebit.app.ssl.TrustingSSLSocketFactory;
import uk.gov.pay.directdebit.common.exception.BadRequestExceptionMapper;
import uk.gov.pay.directdebit.common.exception.ConflictExceptionMapper;
import uk.gov.pay.directdebit.common.exception.InternalServerErrorExceptionMapper;
import uk.gov.pay.directdebit.common.exception.NotFoundExceptionMapper;
import uk.gov.pay.directdebit.gatewayaccounts.GatewayAccountParamConverterProvider;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource;
import uk.gov.pay.directdebit.gatewayaccounts.services.GatewayAccountParser;
import uk.gov.pay.directdebit.gatewayaccounts.services.GatewayAccountService;
import uk.gov.pay.directdebit.healthcheck.resources.HealthCheckResource;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.resources.ConfirmPaymentResource;
import uk.gov.pay.directdebit.mandate.services.PaymentConfirmService;
import uk.gov.pay.directdebit.payers.api.PayerParser;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.resources.PayerResource;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientWrapper;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.resources.PaymentRequestResource;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.PaymentRequestEventService;
import uk.gov.pay.directdebit.payments.services.PaymentRequestService;
import uk.gov.pay.directdebit.payments.services.SandboxService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;
import uk.gov.pay.directdebit.tokens.resources.SecurityTokensResource;
import uk.gov.pay.directdebit.tokens.services.TokenService;
import uk.gov.pay.directdebit.webhook.gocardless.exception.InvalidWebhookExceptionMapper;
import uk.gov.pay.directdebit.webhook.gocardless.resources.WebhookGoCardlessResource;
import uk.gov.pay.directdebit.webhook.sandbox.resources.WebhookSandboxResource;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.util.concurrent.TimeUnit;

import static java.util.EnumSet.of;
import static javax.servlet.DispatcherType.REQUEST;

public class DirectDebitConnectorApp extends Application<DirectDebitConfig> {

    private static final boolean NON_STRICT_VARIABLE_SUBSTITUTOR = false;

    public static void main(String[] args) throws Exception {
        new DirectDebitConnectorApp().run(args);
    }

    @Override
    public void initialize(Bootstrap<DirectDebitConfig> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(NON_STRICT_VARIABLE_SUBSTITUTOR)
                )
        );
        bootstrap.addBundle(new MigrationsBundle<DirectDebitConfig>() {
            @Override
            public DataSourceFactory getDataSourceFactory(DirectDebitConfig configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addCommand(new DependentResourcesWaitCommand());
    }

    @Override
    public void run(DirectDebitConfig configuration, Environment environment) throws Exception {
        final Injector injector = Guice.createInjector(new DirectDebitModule(configuration, environment));

        DataSourceFactory dataSourceFactory = configuration.getDataSourceFactory();
        final DBI jdbi = new DBI(
                dataSourceFactory.getUrl(),
                dataSourceFactory.getUser(),
                dataSourceFactory.getPassword()
        );

        SSLSocketFactory socketFactory = new TrustingSSLSocketFactory();

        // dao
        PaymentRequestDao paymentRequestDao = jdbi.onDemand(PaymentRequestDao.class);
        TokenDao tokenDao = jdbi.onDemand(TokenDao.class);
        PaymentRequestEventDao paymentRequestEventDao = jdbi.onDemand(PaymentRequestEventDao.class);
        TransactionDao transactionDao = jdbi.onDemand(TransactionDao.class);
        PayerDao payerDao = jdbi.onDemand(PayerDao.class);
        MandateDao mandateDao = jdbi.onDemand(MandateDao.class);
        GatewayAccountDao gatewayAccountDao = jdbi.onDemand(GatewayAccountDao.class);
        GoCardlessCustomerDao goCardlessCustomerDao = jdbi.onDemand(GoCardlessCustomerDao.class);
        GoCardlessPaymentDao goCardlessPaymentDao = jdbi.onDemand(GoCardlessPaymentDao.class);
        GoCardlessMandateDao goCardlessMandateDao = jdbi.onDemand(GoCardlessMandateDao.class);

        // clients
        GoCardlessClient goCardlessClient = configuration.getGoCardless().buildClient();
        //fixme there's an ongoing conversation with gocardless to avoid having to do this
        if (configuration.getGoCardless().isCallingStubs()) {
            hackGoCardlessClient(configuration, goCardlessClient, socketFactory);
        }
        GoCardlessClientWrapper goCardlessClientWrapper = new GoCardlessClientWrapper(goCardlessClient);

        //services
        PaymentRequestEventService paymentRequestEventService = new PaymentRequestEventService(paymentRequestEventDao);
        TransactionService transactionService = new TransactionService(transactionDao, paymentRequestEventService);
        TokenService tokenService = new TokenService(tokenDao, transactionService);
        PaymentRequestService paymentRequestService = new PaymentRequestService(
                configuration,
                paymentRequestDao,
                tokenService,
                transactionService,
                gatewayAccountDao);
        GatewayAccountParser gatewayAccountParser = new GatewayAccountParser();
        GatewayAccountService gatewayAccountService = new GatewayAccountService(gatewayAccountDao, gatewayAccountParser);
        PaymentConfirmService paymentConfirmService = new PaymentConfirmService(transactionService, payerDao, mandateDao);
        PayerParser payerParser = new PayerParser();
        PayerService payerService = new PayerService(payerDao, transactionService, payerParser);
        GoCardlessService goCardlessService = new GoCardlessService(payerService,paymentConfirmService, goCardlessClientWrapper, goCardlessCustomerDao, goCardlessPaymentDao, goCardlessMandateDao);
        SandboxService sandboxService = new SandboxService(payerService, paymentConfirmService);
        PaymentProviderFactory paymentProviderFactory = new PaymentProviderFactory(sandboxService, goCardlessService);


        environment.servlets().addFilter("LoggingFilter", new LoggingFilter())
                .addMappingForUrlPatterns(of(REQUEST), true, "/v1/*");
        environment.healthChecks().register("ping", new Ping());
        environment.healthChecks().register("database", injector.getInstance(Database.class));
        environment.jersey().register(new GatewayAccountParamConverterProvider(gatewayAccountDao));

        jdbi.registerContainerFactory(new OptionalContainerFactory());
        environment.jersey().register(injector.getInstance(HealthCheckResource.class));
        environment.jersey().register(injector.getInstance(WebhookGoCardlessResource.class));
        environment.jersey().register(new WebhookSandboxResource(transactionService));
        environment.jersey().register(new PaymentRequestResource(paymentRequestService));
        environment.jersey().register(new SecurityTokensResource(tokenService));
        environment.jersey().register(new PayerResource(paymentProviderFactory));
        environment.jersey().register(new ConfirmPaymentResource(paymentProviderFactory));
        environment.jersey().register(new GatewayAccountResource(gatewayAccountService));
        environment.jersey().register(new InvalidWebhookExceptionMapper());
        environment.jersey().register(new BadRequestExceptionMapper());
        environment.jersey().register(new NotFoundExceptionMapper());
        environment.jersey().register(new ConflictExceptionMapper());
        environment.jersey().register(new InternalServerErrorExceptionMapper());
        setupSSL(configuration, socketFactory);
        initialiseMetrics(configuration, environment);
    }

    private void setupSSL(DirectDebitConfig configuration, SSLSocketFactory sslSocketFactory) {
        HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        System.setProperty("https.proxyHost", configuration.getProxyConfig().getHost());
        System.setProperty("https.proxyPort", configuration.getProxyConfig().getPort().toString());
    }

    // Nasty hack alert - gocardless client does not seem to pick up the certificates in our trust store automatically, so we need to inject those. The underlying client (okhttp) supports that, but it's not accessible. So we make it accessible *** godmode ***. Sent an email to gocardless about the issue.
    private void hackGoCardlessClient(DirectDebitConfig configuration, GoCardlessClient goCardlessClient, SSLSocketFactory sslSocketFactory) throws IllegalAccessException {
        Object httpClient = FieldUtils.readField(goCardlessClient, "httpClient", true);
        OkHttpClient rawClient = (OkHttpClient) FieldUtils.readField(httpClient, "rawClient", true);
        rawClient.setSslSocketFactory(sslSocketFactory);
    }

    private void initialiseMetrics(DirectDebitConfig configuration, Environment environment) {
        GraphiteConfig graphiteConfig = configuration.getGraphiteConfig();
        GraphiteSender graphiteUDP = new GraphiteUDP(graphiteConfig.getHost(), graphiteConfig.getPort());
        GraphiteReporter.forRegistry(environment.metrics())
                .prefixedWith(graphiteConfig.getNode())
                .build(graphiteUDP)
                .start(graphiteConfig.getSendingPeriod(), TimeUnit.SECONDS);
    }
}

