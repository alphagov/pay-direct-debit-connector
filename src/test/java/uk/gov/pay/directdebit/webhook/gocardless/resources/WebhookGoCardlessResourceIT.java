package uk.gov.pay.directdebit.webhook.gocardless.resources;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture.aGoCardlessPaymentFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class WebhookGoCardlessResourceIT {


    @DropwizardTestContext
    private TestContext testContext;

    @Test
    public void handleWebhook_shouldInsertGoCardlessEventsUpdateChargeAndReturn200() throws Exception {
        GatewayAccountFixture testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
        PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                .withGatewayAccountId(testGatewayAccount.getId())
                .insert(testContext.getJdbi());
        TransactionFixture transactionFixture = aTransactionFixture()
                .withPaymentRequestId(paymentRequestFixture.getId())
                .withPaymentRequestExternalId(paymentRequestFixture.getExternalId())
                .withState(PaymentState.PENDING_DIRECT_DEBIT_PAYMENT)
                .insert(testContext.getJdbi());
        aGoCardlessPaymentFixture()
                .withPaymentId("PM00008Q30R2BR")
                .withTransactionId(transactionFixture.getId())
                .insert(testContext.getJdbi());

        given().port(testContext.getPort())
                .body(WEBHOOK)
                .header("Webhook-Signature", WEBHOOK_SIGNATURE)
                .accept(APPLICATION_JSON)
                .post(REQUEST_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        List<Map<String, Object>> events = testContext.getDatabaseTestHelper().getAllGoCardlessEvents();

        Map<String, Object> firstEvent = events.get(0);
        assertThat(firstEvent.get("event_id"), is("EV0000ED6V59V1"));
        assertThat(firstEvent.get("resource_type"), is("PAYMENTS"));
        assertThat(firstEvent.get("action"), is("submitted"));

        Map<String, Object> secondEvent = events.get(1);
        assertThat(secondEvent.get("event_id"), is("EV0000ED6WBEQ0"));
        assertThat(secondEvent.get("resource_type"), is("PAYMENTS"));
        assertThat(secondEvent.get("action"), is("paid_out"));

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        MatcherAssert.assertThat(transaction.get("state"), is("SUCCESS"));
    }
    private final String REQUEST_PATH = "/v1/webhooks/gocardless";

    private final String WEBHOOK = "{\"events\":[{\"id\":\"EV0000ED6V59V1\",\"created_at\":\"2015-04-17T15:24:26.817Z\",\"resource_type\":\"payments\",\"action\":\"submitted\",\"links\":{\"payment\":\"PM00008Q30R2BR\"},\"details\":{\"origin\":\"gocardless\",\"cause\":\"payment_submitted\",\"description\":\"The payment has now been submitted to the banks, and cannot be cancelled. [SANDBOX TRANSITION]\"},\"metadata\":{}},{\"id\":\"EV0000ED6WBEQ0\",\"created_at\":\"2015-04-17T15:24:26.848Z\",\"resource_type\":\"payments\",\"action\":\"paid_out\",\"links\":{\"payment\":\"PM00008Q30R2BR\"},\"details\":{\"origin\":\"gocardless\",\"cause\":\"paid_out\",\"description\":\"Enough time has passed since the payment was submitted for the banks to return an error, so this payment is now confirmed. [SANDBOX TRANSITION]\"},\"metadata\":{}}]}";

    private final String WEBHOOK_SIGNATURE = "0d7cd9d11ad20814124f35dfa3aa9e24fbbbcfaf16b1cd09f04ebe0ca0a47c40";

}
