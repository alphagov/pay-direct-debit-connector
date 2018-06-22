package uk.gov.pay.directdebit.pact;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import uk.gov.pay.commons.testing.pact.providers.PayPactRunner;
import uk.gov.pay.directdebit.junit.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;

import java.util.Map;

@RunWith(PayPactRunner.class)
@Provider("direct-debit-connector")
@PactBroker(protocol = "https", host = "pact-broker-test.cloudapps.digital", port = "443", tags = {"${PACT_CONSUMER_TAG}"},
        authentication = @PactBrokerAuth(username = "${PACT_BROKER_USERNAME}", password = "${PACT_BROKER_PASSWORD}"))
//@PactFolder("pacts") <-- this is useful for testing pacts locally
public class PublicApiContractTest {

    @ClassRule
    public static DropwizardAppWithPostgresRule app = new DropwizardAppWithPostgresRule();
    
    @TestTarget
    public static Target target;

    private GatewayAccountFixture testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture();
    private MandateFixture testMandate = MandateFixture.aMandateFixture();

    @BeforeClass
    public static void setUpService() {
        target = new HttpTarget(app.getLocalPort());
    }
    
    @State("a gateway account with external id exists")
    public void aGatewayAccountWithExternalIdExists(Map<String, String> params) {
        testGatewayAccount.withExternalId(params.get("gateway_account_id")).insert(app.getTestContext().getJdbi());
    }

    @State("a gateway account with external id and a mandate with external id exist")
    public void aGatewayAccountWithExternalIdAndAMandateWithExternalIdExist(Map<String, String> params) {
        testGatewayAccount.withExternalId(params.get("gateway_account_id")).insert(app.getTestContext().getJdbi());
        testMandate.withGatewayAccountFixture(testGatewayAccount).withExternalId(params.get("mandate_id")).insert(app.getTestContext().getJdbi());
    }
    
    @State("three transaction records exist")
    public void threeTransactionRecordsExist(Map<String, String> params) {
        testGatewayAccount.withExternalId(params.get("gateway_account_id")).insert(app.getTestContext().getJdbi());
        MandateFixture testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .withExternalId(params.get("mandate_id"));
        testMandate.insert(app.getTestContext().getJdbi());
        PayerFixture testPayer = PayerFixture.aPayerFixture().withMandateId(testMandate.getId());
        testPayer.insert(app.getTestContext().getJdbi());
        for (int x = 0; x < 3; x++) {
            TransactionFixture.aTransactionFixture().withMandateFixture(testMandate).insert(app.getTestContext().getJdbi());
        }
    }
}
