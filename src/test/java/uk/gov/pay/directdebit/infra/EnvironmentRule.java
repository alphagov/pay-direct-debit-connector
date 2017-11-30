package uk.gov.pay.directdebit.infra;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.rules.RuleChain.outerRule;
import static uk.gov.pay.directdebit.infra.PostgresContainer.DB_PASSWORD;
import static uk.gov.pay.directdebit.infra.PostgresContainer.DB_USERNAME;
import static uk.gov.pay.directdebit.infra.PostgresDockerRule.DB_NAME;

public class EnvironmentRule implements TestRule {

    private final DropwizardAppRule appRule;
    private final RuleChain rules;

    public EnvironmentRule(ConfigOverride... configOverrides) {
        this("config/test-it-config.yaml", configOverrides);
    }

    private EnvironmentRule(String configPath, ConfigOverride... configOverrides) {
        PostgresDockerRule postgres = new PostgresDockerRule();
        appRule = new DropwizardAppRule(DirectDebitConnectorApp.class,
                resourceFilePath(configPath),
                overrideDatabaseUrl(configOverrides, postgres.getDbRootUri() + DB_NAME));
        rules = outerRule(postgres).around(appRule);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return rules.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                restoreDropwizardsLogging();
                base.evaluate();
            }
        }, description);
    }

    public int getPort() {
        return appRule.getLocalPort();
    }

    public DirectDebitConfig getConfiguration() {
        return ((DirectDebitConfig) appRule.getConfiguration());
    }

    private ConfigOverride[] overrideDatabaseUrl(ConfigOverride[] configOverrides, String databaseUrl) {
        List<ConfigOverride> overrides = newArrayList(configOverrides);
        overrides.add(config("database.url", databaseUrl));
        overrides.add(config("database.user", DB_USERNAME));
        overrides.add(config("database.password", DB_PASSWORD));
        return overrides.toArray(new ConfigOverride[0]);
    }

    private void restoreDropwizardsLogging() {
        appRule.getConfiguration().getLoggingFactory()
                .configure(appRule.getEnvironment().metrics(), appRule.getApplication().getName());
    }
}
