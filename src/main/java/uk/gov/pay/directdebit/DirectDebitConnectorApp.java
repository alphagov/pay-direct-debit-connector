package uk.gov.pay.directdebit;

import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.GraphiteUDP;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
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
import uk.gov.pay.directdebit.common.exception.PreconditionFailedExceptionMapper;
import uk.gov.pay.directdebit.events.resources.DirectDebitEventsResource;
import uk.gov.pay.directdebit.gatewayaccounts.GatewayAccountParamConverterProvider;
import uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource;
import uk.gov.pay.directdebit.healthcheck.resources.HealthCheckResource;
import uk.gov.pay.directdebit.mandate.resources.MandateResource;
import uk.gov.pay.directdebit.payers.resources.PayerResource;
import uk.gov.pay.directdebit.payments.resources.PaymentViewResource;
import uk.gov.pay.directdebit.payments.resources.TransactionResource;
import uk.gov.pay.directdebit.tasks.resources.ExpireResource;
import uk.gov.pay.directdebit.tokens.resources.SecurityTokensResource;
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
        DataSourceFactory dataSourceFactory = configuration.getDataSourceFactory();
        final Jdbi jdbi = createJdbi(dataSourceFactory);

        SSLSocketFactory socketFactory = new TrustingSSLSocketFactory();
        final Injector injector = Guice.createInjector(new DirectDebitModule(configuration, environment, jdbi, socketFactory));

        environment.servlets().addFilter("LoggingFilter", new LoggingFilter())
                .addMappingForUrlPatterns(of(REQUEST), true, "/v1/*");
        environment.healthChecks().register("ping", new Ping());
        environment.healthChecks().register("database", injector.getInstance(Database.class));
        environment.jersey().register(injector.getInstance(GatewayAccountParamConverterProvider.class));
        environment.jersey().register(injector.getInstance(HealthCheckResource.class));
        environment.jersey().register(injector.getInstance(WebhookSandboxResource.class));
        environment.jersey().register(injector.getInstance(WebhookGoCardlessResource.class));
        environment.jersey().register(injector.getInstance(SecurityTokensResource.class));
        environment.jersey().register(injector.getInstance(PayerResource.class));
        environment.jersey().register(injector.getInstance(GatewayAccountResource.class));
        environment.jersey().register(injector.getInstance(TransactionResource.class));
        environment.jersey().register(injector.getInstance(PaymentViewResource.class));
        environment.jersey().register(injector.getInstance(MandateResource.class));
        environment.jersey().register(injector.getInstance(ExpireResource.class));
        environment.jersey().register(injector.getInstance(DirectDebitEventsResource.class));


        environment.jersey().register(new InvalidWebhookExceptionMapper());
        environment.jersey().register(new BadRequestExceptionMapper());
        environment.jersey().register(new NotFoundExceptionMapper());
        environment.jersey().register(new ConflictExceptionMapper());
        environment.jersey().register(new InternalServerErrorExceptionMapper());
        environment.jersey().register(new PreconditionFailedExceptionMapper());
        setupSSL(configuration, socketFactory);
        initialiseMetrics(configuration, environment);
    }

    private Jdbi createJdbi(DataSourceFactory dataSourceFactory) {
        final Jdbi jdbi = Jdbi.create(
                dataSourceFactory.getUrl(),
                dataSourceFactory.getUser(),
                dataSourceFactory.getPassword()
        );
        jdbi.installPlugin(new SqlObjectPlugin());

        return jdbi;
    }

    @Inject
    private void setupSSL(DirectDebitConfig configuration, SSLSocketFactory sslSocketFactory) {
        HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        System.setProperty("https.proxyHost", configuration.getProxyConfig().getHost());
        System.setProperty("https.proxyPort", configuration.getProxyConfig().getPort().toString());
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

