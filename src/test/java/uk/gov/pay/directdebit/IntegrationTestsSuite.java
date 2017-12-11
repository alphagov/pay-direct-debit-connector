package uk.gov.pay.directdebit;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.gov.pay.directdebit.infra.EnvironmentRule;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDaoIT;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDaoIT;
import uk.gov.pay.directdebit.payments.dao.TransactionDaoIT;
import uk.gov.pay.directdebit.payments.resources.PaymentRequestResourceIT;
import uk.gov.pay.directdebit.resources.HealthCheckResourceIT;
import uk.gov.pay.directdebit.tokens.dao.TokenDaoIT;
import uk.gov.pay.directdebit.tokens.resources.SecurityTokensResourceIT;

import static uk.gov.pay.directdebit.infra.PostgresTemplate.createTemplate;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        PaymentRequestDaoIT.class,
        PaymentRequestEventDaoIT.class,
        TokenDaoIT.class,
        TransactionDaoIT.class,
        HealthCheckResourceIT.class,
        PaymentRequestResourceIT.class,
        SecurityTokensResourceIT.class
})
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
