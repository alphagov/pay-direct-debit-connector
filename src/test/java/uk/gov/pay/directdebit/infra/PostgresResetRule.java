package uk.gov.pay.directdebit.infra;

import io.dropwizard.db.DataSourceFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static uk.gov.pay.directdebit.infra.PostgresTemplate.restoreTemplate;

public class PostgresResetRule implements TestRule {

    private final DataSourceFactory dbConfig;

    public PostgresResetRule(DataSourceFactory datasourceConfig) {
        dbConfig = datasourceConfig;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        restoreTemplate(dbConfig);
        return statement;
    }
}
