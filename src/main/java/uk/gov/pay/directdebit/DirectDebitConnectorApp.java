package uk.gov.pay.directdebit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.pay.directdebit.app.bootstrap.DependentResourcesWaitCommand;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.DirectDebitModule;
import uk.gov.pay.directdebit.app.filters.LoggingFilter;
import uk.gov.pay.directdebit.app.healthcheck.Database;
import uk.gov.pay.directdebit.app.healthcheck.Ping;
import uk.gov.pay.directdebit.app.ssl.TrustingSSLSocketFactory;
import uk.gov.pay.directdebit.common.resources.V1ApiPaths;
import uk.gov.pay.directdebit.healthcheck.resources.HealthCheckResource;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionExceptionMapper;
import uk.gov.pay.directdebit.webhook.gocardless.exception.InvalidWebhookExceptionMapper;
import uk.gov.pay.directdebit.webhook.gocardless.resources.WebhookGoCardlessResource;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import static java.util.EnumSet.of;
import static javax.servlet.DispatcherType.REQUEST;

public class DirectDebitConnectorApp extends Application<DirectDebitConfig> {

    private static final boolean NON_STRICT_VARIABLE_SUBSTITUTOR = false;

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

        environment.servlets().addFilter("LoggingFilter", new LoggingFilter())
                .addMappingForUrlPatterns(of(REQUEST), true, V1ApiPaths.ROOT_PATH + "/*");

        environment.healthChecks().register("ping", new Ping());
        environment.healthChecks().register("database", injector.getInstance(Database.class));

        environment.jersey().register(injector.getInstance(HealthCheckResource.class));
        environment.jersey().register(injector.getInstance(WebhookGoCardlessResource.class));
        environment.jersey().register(new InvalidWebhookExceptionMapper());
        environment.jersey().register(new InvalidStateTransitionExceptionMapper());
        setupSSL();
    }

    private void setupSSL() {
        SSLSocketFactory socketFactory = new TrustingSSLSocketFactory();
        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
    }

    public static void main(String[] args) throws Exception {
        new DirectDebitConnectorApp().run(args);
    }
}
