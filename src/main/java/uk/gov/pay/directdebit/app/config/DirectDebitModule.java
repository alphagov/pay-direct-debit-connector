package uk.gov.pay.directdebit.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gocardless.GoCardlessClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.dropwizard.setup.Environment;
import uk.gov.pay.directdebit.app.core.WebhookVerifier;

public class DirectDebitModule extends AbstractModule {

    final DirectDebitConfig configuration;
    final Environment environment;

    public DirectDebitModule(final DirectDebitConfig configuration, final Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(DirectDebitConfig.class).toInstance(configuration);
        bind(Environment.class).toInstance(environment);

//        install(jpaModule(configuration));
    }

//TODO: Awaiting AWS DB environment ready
//    private JpaPersistModule jpaModule(DirectDebitConfig configuration) {
//        DataSourceFactory dbConfig = configuration.getDataSourceFactory();
//        final Properties properties = new Properties();
//        properties.put("javax.persistence.jdbc.driver", dbConfig.getDriverClass());
//        properties.put("javax.persistence.jdbc.url", dbConfig.getUrl());
//        properties.put("javax.persistence.jdbc.user", dbConfig.getUser());
//        properties.put("javax.persistence.jdbc.password", dbConfig.getPassword());
//
//        JPAConfiguration jpaConfiguration = configuration.getJpaConfiguration();
//        properties.put("eclipselink.logging.level", jpaConfiguration.getJpaLoggingLevel());
//        properties.put("eclipselink.logging.level.sql", jpaConfiguration.getSqlLoggingLevel());
//        properties.put("eclipselink.query-results-cache", jpaConfiguration.getCacheSharedDefault());
//        properties.put("eclipselink.cache.shared.default", jpaConfiguration.getCacheSharedDefault());
//        properties.put("eclipselink.ddl-generation.output-mode", jpaConfiguration.getDdlGenerationOutputMode());
//        properties.put("eclipselink.session.customizer", "uk.gov.pay.directdebit.app.config.DirectDebitSessionCustomiser");
//
//        final JpaPersistModule jpaModule = new JpaPersistModule("DirectDebitConnectorUnit");
//        jpaModule.properties(properties);
//
//        return jpaModule;
//    }

    @Provides
    public GoCardlessClient provideGoCardlessClient() {
        return configuration.getGoCardless().buildClient();
    }

    @Provides
    public WebhookVerifier provideWebhookVerifier() {
        return configuration.getGoCardless().buildSignatureVerifier();
    }

    @Provides
    public ObjectMapper provideObjectMapper() {
        return environment.getObjectMapper();
    }

}
