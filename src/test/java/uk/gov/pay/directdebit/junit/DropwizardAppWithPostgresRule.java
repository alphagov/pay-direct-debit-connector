package uk.gov.pay.directdebit.junit;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static uk.gov.pay.directdebit.junit.PostgresTemplate.createTemplate;
import static uk.gov.pay.directdebit.junit.PostgresTestContainer.DB_PASSWORD;
import static uk.gov.pay.directdebit.junit.PostgresTestContainer.DB_USERNAME;
import static uk.gov.pay.directdebit.junit.PostgresTestDocker.getDbUri;
import static uk.gov.pay.directdebit.junit.PostgresTestDocker.getOrCreate;

public class DropwizardAppWithPostgresRule implements TestRule {

    private final DropwizardAppRule<DirectDebitConfig> app;
    private final String configFilePath;
    private final RuleChain rules;
    
    private TestContext testContext;

    public DropwizardAppWithPostgresRule() {
        configFilePath = resourceFilePath("config/test-it-config.yaml");
        getOrCreate("govukpay/postgres:9.4.4");
        ConfigOverride[] configOverride = {config("database.url", getDbUri()), config("database.user", DB_USERNAME), config("database.password", DB_PASSWORD)};
        app = new DropwizardAppRule<>(
                DirectDebitConnectorApp.class,
                configFilePath,
                configOverride);
        rules = RuleChain.outerRule(app);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return rules.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                app.getApplication().run("db", "migrate", configFilePath);
                createTemplate(getDbUri(), DB_USERNAME, DB_PASSWORD);
                testContext = new TestContext(app.getLocalPort(), ((DirectDebitConfig) app.getConfiguration()).getDataSourceFactory());
                base.evaluate();
            }
        }, description);
    }

    public int getLocalPort() {
        return app.getLocalPort();
    }

    public TestContext getTestContext() {
        return testContext;
    }
}
