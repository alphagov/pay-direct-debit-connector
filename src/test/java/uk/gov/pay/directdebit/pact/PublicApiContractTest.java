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
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.junit.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.time.ZonedDateTime;
import java.util.Map;

import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

@RunWith(PactRunner.class)
@Provider("direct-debit-connector")
@PactBroker(scheme = "https", host = "pact-broker-test.cloudapps.digital", tags = {"master", "test", "staging", "production"},
        authentication = @PactBrokerAuth(username = "${PACT_BROKER_USERNAME}", password = "${PACT_BROKER_PASSWORD}"),
        consumers = {"publicapi"})
//uncommenting the below is useful for testing pacts locally. grab the pact from the broker and put it in /pacts
//@PactFolder("pacts")
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

    @Before
    public void resetDatabase() {
        app.getDatabaseTestHelper().truncateAllData();
    }

    @State("a gateway account with external id exists")
    public void aGatewayAccountWithExternalIdExists(Map<String, String> params) {
        testGatewayAccount.withExternalId(params.get("gateway_account_id")).insert(app.getTestContext().getJdbi());
    }

    @State("a gateway account with external id and a mandate with external id exist")
    public void aGatewayAccountWithExternalIdAndAMandateWithExternalIdExist(Map<String, String> params) {
        testGatewayAccount.withExternalId(params.get("gateway_account_id")).insert(app.getTestContext().getJdbi());
        testMandate.withGatewayAccountFixture(testGatewayAccount)
                .withExternalId(MandateExternalId.valueOf(params.get("mandate_id")))
                .withPayerFixture(PayerFixture.aPayerFixture())
                .withState(MandateState.SUBMITTED_TO_PROVIDER)
                .insert(app.getTestContext().getJdbi());
    }

    @State("a gateway account with external id and a confirmed mandate exists")
    public void aGatewayAccountWithExternalIdAndAConfirmedMandateExists(Map<String, String> params) {
        testGatewayAccount.withExternalId(params.get("gateway_account_id")).insert(app.getTestContext().getJdbi());
        testMandate.withGatewayAccountFixture(testGatewayAccount)
                .withExternalId(MandateExternalId.valueOf(params.get("mandate_id")))
                .withPayerFixture(PayerFixture.aPayerFixture())
                .withMandateBankStatementReference(MandateBankStatementReference.valueOf(params.get("bank_mandate_reference")))
                .withPaymentProviderId(GoCardlessMandateId.valueOf(params.get("unique_identifier")))
                .withState(MandateState.SUBMITTED_TO_PROVIDER)
                .withStateDetails("mandate_state_details")
                .insert(app.getTestContext().getJdbi());
    }

    @State("three payment records exist")
    public void threePaymentRecordsExist(Map<String, String> params) {
        testGatewayAccount.withPaymentProvider(PaymentProvider.GOCARDLESS)
                .withExternalId(params.get("gateway_account_id"))
                .insert(app.getTestContext().getJdbi());
        MandateFixture testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .withPaymentProviderId(GoCardlessMandateId.valueOf("MD1"))
                .withExternalId(MandateExternalId.valueOf(params.get("mandate_id")));
        testMandate.insert(app.getTestContext().getJdbi());
        PayerFixture testPayer = PayerFixture.aPayerFixture().withMandateId(testMandate.getId());
        testPayer.insert(app.getTestContext().getJdbi());
        for (int x = 0; x < 3; x++) {
            aPaymentFixture().withStateDetails("payment_state_details").withMandateFixture(testMandate).insert(app.getTestContext().getJdbi());
        }
    }

    @State("a gateway account with external id and a confirmed mandate with a payment on it exists")
    public void aValidPaymentExists(Map<String, String> params) {
        testGatewayAccount.withExternalId(params.get("gateway_account_id")).insert(app.getTestContext().getJdbi());
        testMandate.withGatewayAccountFixture(testGatewayAccount)
                .withExternalId(MandateExternalId.valueOf(params.get("mandate_id")))
                .withPayerFixture(PayerFixture.aPayerFixture())
                .withPaymentProviderId(GoCardlessMandateId.valueOf(params.get("unique_identifier")))
                .withState(MandateState.SUBMITTED_TO_PROVIDER)
                .insert(app.getTestContext().getJdbi());
        aPaymentFixture()
                .withMandateFixture(testMandate)
                .withExternalId(params.get("charge_id"))
                .withAmount(1000L)
                .withReference("ABCDE")
                .withState(PaymentState.SUBMITTED_TO_PROVIDER)
                .withStateDetails("payment_state_details")
                .withCreatedDate(ZonedDateTime.parse("1995-10-27T10:21:01.499Z"))
                .withPaymentProviderId(GoCardlessPaymentId.valueOf("AAAA1111"))
                .insert(app.getTestContext().getJdbi());
        
    }
}
