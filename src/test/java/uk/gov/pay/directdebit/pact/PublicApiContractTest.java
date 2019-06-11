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
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.junit.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

@RunWith(PactRunner.class)
@Provider("direct-debit-connector")
@PactBroker(scheme = "https", host = "pact-broker-test.cloudapps.digital", tags = {"${PACT_CONSUMER_TAG}", "test", "staging", "production"},
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
                .insert(app.getTestContext().getJdbi());
    }

    @State("three transaction records exist")
    public void threeTransactionRecordsExist(Map<String, String> params) {
        testGatewayAccount.withExternalId(params.get("gateway_account_id")).insert(app.getTestContext().getJdbi());
        MandateFixture testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .withExternalId(MandateExternalId.valueOf(params.get("agreement_id")));
        testMandate.insert(app.getTestContext().getJdbi());
        PayerFixture testPayer = PayerFixture.aPayerFixture().withMandateId(testMandate.getId());
        testPayer.insert(app.getTestContext().getJdbi());
        for (int x = 0; x < 3; x++) {
            TransactionFixture.aTransactionFixture().withMandateFixture(testMandate).insert(app.getTestContext().getJdbi());
        }
    }

    @State("a direct debit event exists")
    public void aDirectDebitEventExists(Map<String, String> params) {

        String transactionExternalId = params.getOrDefault("transaction_external_id", RandomIdGenerator.newId());
        MandateExternalId mandateExternalId = MandateExternalId.valueOf(params.getOrDefault("mandate_external_id", RandomIdGenerator.newId()));

        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
                .insert(app.getTestContext().getJdbi());

        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(mandateExternalId);

        MandateDao mandateDao = app.getTestContext().getJdbi().onDemand(MandateDao.class);
        Optional<Mandate> mandateByExternalId = mandateDao.findByExternalId(mandateExternalId);
        if (!mandateByExternalId.isPresent()) {
            mandateFixture.insert(app.getTestContext().getJdbi());
        } else {
            mandateFixture.withId(mandateByExternalId.get().getId());
        }

        TransactionFixture transaction = TransactionFixture.aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withExternalId(transactionExternalId)
                .insert(app.getTestContext().getJdbi());

        Long eventId = Long.valueOf(params.get("event_id"));
        DirectDebitEventFixture.aDirectDebitEventFixture()
                .withId(eventId)
                .withMandateId(mandateFixture.getId())
                .withTransactionId(transaction.getId())
                .withEvent(DirectDebitEvent.SupportedEvent.valueOf(params.get("event")))
                .withEventType(DirectDebitEvent.Type.valueOf(params.get("event_type")))
                .withEventDate(ZonedDateTime.parse(params.getOrDefault("event_date", ZonedDateTime.now().toString())))
                .withExternalId(params.getOrDefault("external_id", RandomIdGenerator.newId()))
                .insert(app.getTestContext().getJdbi());
    }
}
