package uk.gov.pay.directdebit;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.gov.pay.directdebit.infra.EnvironmentRule;
import uk.gov.pay.directdebit.resources.HealthCheckResourceIT;

import static uk.gov.pay.directdebit.infra.PostgresTemplate.createTemplate;

@RunWith(Suite.class)
@Suite.SuiteClasses({HealthCheckResourceIT.class})
public class IntegrationTestsSuite {

    @ClassRule
    public static EnvironmentRule app = new EnvironmentRule();

    @BeforeClass
    public static void createPostgresTemplate() throws Exception {
        createTemplate(app.getConfiguration().getDataSourceFactory());
    }

    public static EnvironmentRule env() {
        return app;
    }
}
