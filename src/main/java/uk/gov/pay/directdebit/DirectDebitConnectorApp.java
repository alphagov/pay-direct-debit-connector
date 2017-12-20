package uk.gov.pay.directdebit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.OptionalContainerFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.app.bootstrap.DependentResourcesWaitCommand;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.DirectDebitModule;
import uk.gov.pay.directdebit.app.filters.LoggingFilter;
import uk.gov.pay.directdebit.app.healthcheck.Database;
import uk.gov.pay.directdebit.app.healthcheck.Ping;
import uk.gov.pay.directdebit.app.ssl.TrustingSSLSocketFactory;
import uk.gov.pay.directdebit.common.exception.BadRequestExceptionMapper;
import uk.gov.pay.directdebit.common.exception.ConflictExceptionMapper;
import uk.gov.pay.directdebit.common.exception.NotFoundExceptionMapper;
import uk.gov.pay.directdebit.healthcheck.resources.HealthCheckResource;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.resources.ConfirmPaymentResource;
import uk.gov.pay.directdebit.mandate.services.PaymentConfirmService;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.resources.PayerResource;
import uk.gov.pay.directdebit.payers.services.PayerParser;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.resources.PaymentRequestResource;
import uk.gov.pay.directdebit.payments.services.PaymentRequestEventService;
import uk.gov.pay.directdebit.payments.services.PaymentRequestService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;
import uk.gov.pay.directdebit.tokens.resources.SecurityTokensResource;
import uk.gov.pay.directdebit.tokens.services.TokenService;
import uk.gov.pay.directdebit.webhook.gocardless.exception.InvalidWebhookExceptionMapper;
import uk.gov.pay.directdebit.webhook.gocardless.resources.WebhookGoCardlessResource;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

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


        PaymentRequestDao paymentRequestDao = jdbi.onDemand(PaymentRequestDao.class);
        TokenDao tokenDao = jdbi.onDemand(TokenDao.class);
        PaymentRequestEventDao paymentRequestEventDao = jdbi.onDemand(PaymentRequestEventDao.class);
        TransactionDao transactionDao = jdbi.onDemand(TransactionDao.class);
        PayerDao payerDao = jdbi.onDemand(PayerDao.class);
        MandateDao mandateDao = jdbi.onDemand(MandateDao.class);
        PaymentRequestEventService paymentRequestEventService = new PaymentRequestEventService(paymentRequestEventDao);
        TransactionService transactionService = new TransactionService(transactionDao, paymentRequestEventService);
        TokenService tokenService = new TokenService(tokenDao, transactionService);
        PaymentRequestService paymentRequestService = new PaymentRequestService(
                configuration,
                paymentRequestDao,
                tokenService,
                transactionService);

        PayerParser payerParser = new PayerParser();
        PayerService payerService = new PayerService(payerDao, transactionService, payerParser);
        environment.servlets().addFilter("LoggingFilter", new LoggingFilter())
                .addMappingForUrlPatterns(of(REQUEST), true, "/v1/*");


        environment.healthChecks().register("ping", new Ping());
        environment.healthChecks().register("database", injector.getInstance(Database.class));

        jdbi.registerContainerFactory(new OptionalContainerFactory());
        environment.jersey().register(injector.getInstance(HealthCheckResource.class));
        environment.jersey().register(injector.getInstance(WebhookGoCardlessResource.class));
        environment.jersey().register(new PaymentRequestResource(paymentRequestService));
        environment.jersey().register(new SecurityTokensResource(tokenService));
        environment.jersey().register(new PayerResource(payerService));
        environment.jersey().register(new ConfirmPaymentResource(new PaymentConfirmService(transactionService, payerDao, mandateDao)));
        environment.jersey().register(new InvalidWebhookExceptionMapper());
        environment.jersey().register(new BadRequestExceptionMapper());
        environment.jersey().register(new NotFoundExceptionMapper());
        environment.jersey().register(new ConflictExceptionMapper());
        setupSSL(configuration);
    }

    private void setupSSL(DirectDebitConfig configuration) {
        SSLSocketFactory socketFactory = new TrustingSSLSocketFactory();
        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
        System.setProperty("https.proxyHost", configuration.getProxyConfig().getHost());
        System.setProperty("https.proxyPort", configuration.getProxyConfig().getPort().toString());
    }
}

