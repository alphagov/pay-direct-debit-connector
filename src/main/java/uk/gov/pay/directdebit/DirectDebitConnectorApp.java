package uk.gov.pay.directdebit;

import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.GraphiteUDP;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.commons.utils.logging.LoggingFilter;
import uk.gov.pay.commons.utils.metrics.DatabaseMetricsService;
import uk.gov.pay.directdebit.app.bootstrap.DependentResourcesWaitCommand;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.DirectDebitModule;
import uk.gov.pay.directdebit.app.config.GraphiteConfig;
import uk.gov.pay.directdebit.app.healthcheck.Ping;
import uk.gov.pay.directdebit.common.exception.BadRequestExceptionMapper;
import uk.gov.pay.directdebit.common.exception.ConflictExceptionMapper;
import uk.gov.pay.directdebit.common.exception.GoCardlessAccountAlreadyConnectedExceptionMapper;
import uk.gov.pay.directdebit.common.exception.InternalServerErrorExceptionMapper;
import uk.gov.pay.directdebit.common.exception.JerseyViolationExceptionMapper;
import uk.gov.pay.directdebit.common.exception.JsonMappingExceptionMapper;
import uk.gov.pay.directdebit.common.exception.MandateStateInvalidExceptionMapper;
import uk.gov.pay.directdebit.common.exception.NoAccessTokenExceptionMapper;
import uk.gov.pay.directdebit.common.exception.NotFoundExceptionMapper;
import uk.gov.pay.directdebit.common.exception.PreconditionFailedExceptionMapper;
import uk.gov.pay.directdebit.common.exception.UnlinkedGCMerchantAccountExceptionMapper;
import uk.gov.pay.directdebit.common.proxy.CustomInetSocketAddressProxySelector;
import uk.gov.pay.directdebit.gatewayaccounts.GatewayAccountParamConverterProvider;
import uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource;
import uk.gov.pay.directdebit.healthcheck.resources.HealthCheckResource;
import uk.gov.pay.directdebit.mandate.resources.MandateResource;
import uk.gov.pay.directdebit.partnerapp.resources.GoCardlessAppConnectAccountStateResource;
import uk.gov.pay.directdebit.partnerapp.resources.GoCardlessAppConnectAccountTokenResource;
import uk.gov.pay.directdebit.payers.resources.PayerResource;
import uk.gov.pay.directdebit.payments.resources.PaymentResource;
import uk.gov.pay.directdebit.payments.resources.PaymentSearchResource;
import uk.gov.pay.directdebit.tasks.resources.ExpireResource;
import uk.gov.pay.directdebit.tokens.resources.SecurityTokensResource;
import uk.gov.pay.directdebit.webhook.gocardless.exception.InvalidWebhookExceptionMapper;
import uk.gov.pay.directdebit.webhook.gocardless.resources.WebhookGoCardlessResource;
import uk.gov.pay.directdebit.webhook.sandbox.resources.WebhookSandboxResource;
import uk.gov.pay.logging.LogstashConsoleAppenderFactory;

import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;

import static java.util.EnumSet.of;
import static javax.servlet.DispatcherType.REQUEST;

public class DirectDebitConnectorApp extends Application<DirectDebitConfig> {

    private static final boolean NON_STRICT_VARIABLE_SUBSTITUTOR = false;

    public static void main(String[] args) throws Exception {
        // Initialise java.security options
        java.security.Security.setProperty("networkaddress.cache.ttl", "1");

        // Run the application
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
        bootstrap.addBundle(new JdbiExceptionsBundle());
        bootstrap.addCommand(new DependentResourcesWaitCommand());
        bootstrap.getObjectMapper().getSubtypeResolver().registerSubtypes(LogstashConsoleAppenderFactory.class);
    }

    @Override
    public void run(DirectDebitConfig configuration, Environment environment) {
        if ((System.getProperty("https.proxyHost") != null) && (System.getProperty("https.proxyPort") != null)) {
            CustomInetSocketAddressProxySelector customInetSocketAddressProxySelector =
                    new CustomInetSocketAddressProxySelector(
                            ProxySelector.getDefault(),
                            System.getProperty("https.proxyHost"),
                            Integer.parseInt(System.getProperty("https.proxyPort"))
                    );
            ProxySelector.setDefault(customInetSocketAddressProxySelector);
        }

        final Jdbi jdbi = new JdbiFactory().build(environment, configuration.getDataSourceFactory(), "postgresql");
        final Injector injector = Guice.createInjector(new DirectDebitModule(configuration, environment, jdbi));

        environment.servlets().addFilter("LoggingFilter", new LoggingFilter())
                .addMappingForUrlPatterns(of(REQUEST), true, "/v1/*");
        environment.healthChecks().register("ping", new Ping());
        environment.jersey().register(injector.getInstance(GatewayAccountParamConverterProvider.class));
        environment.jersey().register(injector.getInstance(HealthCheckResource.class));
        environment.jersey().register(injector.getInstance(WebhookSandboxResource.class));
        environment.jersey().register(injector.getInstance(WebhookGoCardlessResource.class));
        environment.jersey().register(injector.getInstance(SecurityTokensResource.class));
        environment.jersey().register(injector.getInstance(PayerResource.class));
        environment.jersey().register(injector.getInstance(GatewayAccountResource.class));
        environment.jersey().register(injector.getInstance(PaymentResource.class));
        environment.jersey().register(injector.getInstance(PaymentSearchResource.class));
        environment.jersey().register(injector.getInstance(MandateResource.class));
        environment.jersey().register(injector.getInstance(ExpireResource.class));
        environment.jersey().register(injector.getInstance(GoCardlessAppConnectAccountStateResource.class));
        environment.jersey().register(injector.getInstance(GoCardlessAppConnectAccountTokenResource.class));

        environment.jersey().register(new JerseyViolationExceptionMapper());
        environment.jersey().register(new InvalidWebhookExceptionMapper());
        environment.jersey().register(new BadRequestExceptionMapper());
        environment.jersey().register(new NotFoundExceptionMapper());
        environment.jersey().register(new ConflictExceptionMapper());
        environment.jersey().register(new InternalServerErrorExceptionMapper());
        environment.jersey().register(new PreconditionFailedExceptionMapper());
        environment.jersey().register(new JsonMappingExceptionMapper());
        environment.jersey().register(new NoAccessTokenExceptionMapper());
        environment.jersey().register(new UnlinkedGCMerchantAccountExceptionMapper());
        environment.jersey().register(new MandateStateInvalidExceptionMapper());
        environment.jersey().register(new GoCardlessAccountAlreadyConnectedExceptionMapper());
        initialiseMetrics(configuration, environment);
    }

    private void initialiseMetrics(DirectDebitConfig configuration, Environment environment) {
        GraphiteConfig graphiteConfig = configuration.getGraphiteConfig();
        DatabaseMetricsService metricsService = new DatabaseMetricsService(configuration.getDataSourceFactory(), environment.metrics(), "directdebit_connector");

        environment
                .lifecycle()
                .scheduledExecutorService("metricscollector")
                .threads(1)
                .build()
                .scheduleAtFixedRate(metricsService::updateMetricData, 0, graphiteConfig.getSendingPeriod() / 2, TimeUnit.SECONDS);

        GraphiteSender graphiteUDP = new GraphiteUDP(graphiteConfig.getHost(), graphiteConfig.getPort());
        GraphiteReporter.forRegistry(environment.metrics())
                .prefixedWith(graphiteConfig.getNode())
                .build(graphiteUDP)
                .start(graphiteConfig.getSendingPeriod(), TimeUnit.SECONDS);
    }
}

