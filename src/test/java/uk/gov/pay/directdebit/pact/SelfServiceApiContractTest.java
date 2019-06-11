package uk.gov.pay.directdebit.pact;

import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.junit.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

@RunWith(PactRunner.class)
@Provider("direct-debit-connector")
@PactBroker(scheme = "https", host = "pact-broker-test.cloudapps.digital", tags = {"${PACT_CONSUMER_TAG}", "test", "staging", "production"},
        authentication = @PactBrokerAuth(username = "${PACT_BROKER_USERNAME}", password = "${PACT_BROKER_PASSWORD}"),
        consumers = {"selfservice"})
public class SelfServiceApiContractTest {

    @ClassRule
    public static DropwizardAppWithPostgresRule app = new DropwizardAppWithPostgresRule();

    @TestTarget
    public static Target target;

    private GatewayAccountFixture testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture();

    @BeforeClass
    public static void setUpService() {
        target = new HttpTarget(app.getLocalPort());
    }

    @Before
    public void resetDatabase() {
        app.getDatabaseTestHelper().truncateAllData();
    }

    @State("Direct Debit gateway account with id 667 exists in the database")
    public void aGatewayAccountWithExternalIdExists() {
        testGatewayAccount.withExternalId("667").insert(app.getTestContext().getJdbi());
    }
}
