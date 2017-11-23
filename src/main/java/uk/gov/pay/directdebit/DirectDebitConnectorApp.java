package uk.gov.pay.directdebit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.DirectDebitModule;
import uk.gov.pay.directdebit.app.filters.LoggingFilter;
import uk.gov.pay.directdebit.app.healthchecks.Ping;
import uk.gov.pay.directdebit.common.resources.V1ApiPaths;
import uk.gov.pay.directdebit.healthcheck.resources.HealthCheckResource;
import uk.gov.pay.directdebit.webhook.gocardless.exception.InvalidWebhookExceptionMapper;
import uk.gov.pay.directdebit.webhook.gocardless.resources.WebhookGoCardlessResource;

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

        //TODO: Awaiting AWS DB environment ready
//        bootstrap.addBundle(new MigrationsBundle<DirectDebitConfig>() {
//            @Override
//            public DataSourceFactory getDataSourceFactory(DirectDebitConfig configuration) {
//                return configuration.getDataSourceFactory();
//            }
//        });
//
//        bootstrap.addCommand(new DependentResourceWaitCommand());
    }

    @Override
    public void run(DirectDebitConfig configuration, Environment environment) throws Exception {
        final Injector injector = Guice.createInjector(new DirectDebitModule(configuration, environment));

        environment.servlets().addFilter("LoggingFilter", new LoggingFilter())
                .addMappingForUrlPatterns(of(REQUEST), true, V1ApiPaths.ROOT_PATH + "/*");
        environment.healthChecks().register("ping", new Ping());
        //TODO: Awaiting AWS DB environment ready
//        injector.getInstance(PersistenceServiceInitialiser.class);
//        environment.healthChecks().register("database", injector.getInstance(DatabaseHealthCheck.class));

        environment.jersey().register(injector.getInstance(HealthCheckResource.class));
        environment.jersey().register(injector.getInstance(WebhookGoCardlessResource.class));

        // Register the custom ExceptionMapper(s)
        environment.jersey().register(new InvalidWebhookExceptionMapper());
    }

    public static void main(String[] args) throws Exception {
        new DirectDebitConnectorApp().run(args);
    }
}
