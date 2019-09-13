package uk.gov.pay.directdebit.junit;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static uk.gov.pay.directdebit.junit.PostgresTestDocker.getDbPassword;
import static uk.gov.pay.directdebit.junit.PostgresTestDocker.getDbUri;
import static uk.gov.pay.directdebit.junit.PostgresTestDocker.getDbUsername;
import static uk.gov.pay.directdebit.junit.PostgresTestDocker.getOrCreate;

public class DropwizardAppWithPostgresRule implements TestRule {

    private final DropwizardAppRule<DirectDebitConfig> app;
    private final String configFilePath;
    private final RuleChain rules;

    private TestContext testContext;
    private DatabaseTestHelper databaseTestHelper;

    public DropwizardAppWithPostgresRule() {
        configFilePath = resourceFilePath("config/test-it-config.yaml");
        getOrCreate();
        ConfigOverride[] configOverride = {config("database.url", getDbUri()), config("database.user", getDbUsername()), config("database.password", getDbPassword())};
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
                testContext = new TestContext(app.getLocalPort(), app.getConfiguration());
                databaseTestHelper = new DatabaseTestHelper(testContext.getJdbi());
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

    public DatabaseTestHelper getDatabaseTestHelper() {
        return databaseTestHelper;
    }
}
