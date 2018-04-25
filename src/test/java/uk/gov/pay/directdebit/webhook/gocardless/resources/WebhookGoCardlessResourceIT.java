package uk.gov.pay.directdebit.webhook.gocardless.resources;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture.aGoCardlessMandateFixture;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture.aGoCardlessPaymentFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class WebhookGoCardlessResourceIT {

    private final static String REQUEST_PATH = "/v1/webhooks/gocardless";
    private final static String WEBHOOK_SUCCESS = "{\"events\":[{\"id\":\"EV0000ED6V59V1\",\"created_at\":\"2015-04-17T15:24:26.817Z\",\"resource_type\":\"payments\",\"action\":\"status_unhandled_in_our_side\",\"links\":{\"payment\":\"PM00008Q30R2BR\"},\"details\":{\"origin\":\"gocardless\",\"cause\":\"payment_submitted\",\"description\":\"The payment has now been submitted to the banks, and cannot be cancelled. [SANDBOX TRANSITION]\"},\"metadata\":{}},{\"id\":\"EV0000ED6WBEQ0\",\"created_at\":\"2015-04-17T15:24:26.848Z\",\"resource_type\":\"payments\",\"action\":\"paid_out\",\"links\":{\"payment\":\"PM00008Q30R2BR\"},\"details\":{\"origin\":\"gocardless\",\"cause\":\"paid_out\",\"description\":\"Enough time has passed since the payment was submitted for the banks to return an error, so this payment is now confirmed. [SANDBOX TRANSITION]\"},\"metadata\":{}}]}";
    private final static String WEBHOOK_SUCCESS_SIGNATURE = "0daa8acb3ea7e5f42bab6ddb0daabeae985cb793b07850e1387be209c1d40001";

    private final static String WEBHOOK_FAILED = "{\"events\":[{\"id\":\"EV0000ED6V59V1\",\"created_at\":\"2015-04-17T15:24:26.817Z\",\"resource_type\":\"payments\",\"action\":\"status_unhandled_in_our_side\",\"links\":{\"payment\":\"PM00008Q30R2BR\"},\"details\":{\"origin\":\"gocardless\",\"cause\":\"payment_submitted\",\"description\":\"The payment has now been submitted to the banks, and cannot be cancelled. [SANDBOX TRANSITION]\"},\"metadata\":{}},{\"id\":\"EV0000ED6WBEQ0\",\"created_at\":\"2015-04-17T15:24:26.848Z\",\"resource_type\":\"mandates\",\"action\":\"failed\",\"links\":{\"mandate\":\"MD00008Q30R2BR\"},\"details\":{\"origin\":\"gocardless\",\"cause\":\"failed\",\"description\":\"Enough time has passed since the payment was submitted for the banks to return an error, so this payment is now confirmed. [SANDBOX TRANSITION]\"},\"metadata\":{}}]}";

    private final static String WEBHOOK_FAILED_SIGNATURE = "eeaad0baef683cf610469efad9fe09809192876d547b22e59249ade8f7ee08b9";

    @DropwizardTestContext
    private TestContext testContext;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(10110);

    @Test
    public void handleWebhook_whenAPaidOutWebhookArrives_shouldInsertGoCardlessEventsUpdatePaymentToSuccessAndReturn200() {
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
                .body(WEBHOOK_SUCCESS)
                .header("Webhook-Signature", WEBHOOK_SUCCESS_SIGNATURE)
                .accept(APPLICATION_JSON)
                .post(REQUEST_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        List<Map<String, Object>> events = testContext.getDatabaseTestHelper().getAllGoCardlessEvents();

        Map<String, Object> firstEvent = events.get(0);
        assertThat(firstEvent.get("event_id"), is("EV0000ED6V59V1"));
        assertThat(firstEvent.get("resource_type"), is("PAYMENTS"));
        assertThat(firstEvent.get("action"), is("status_unhandled_in_our_side"));

        Map<String, Object> secondEvent = events.get(1);
        assertThat(secondEvent.get("event_id"), is("EV0000ED6WBEQ0"));
        assertThat(secondEvent.get("resource_type"), is("PAYMENTS"));
        assertThat(secondEvent.get("action"), is("paid_out"));

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        MatcherAssert.assertThat(transaction.get("state"), is("SUCCESS"));
    }

    @Test
    public void handleWebhook_whenAMandateFailedWebhookArrives_shouldInsertGoCardlessEventsUpdatePaymentToFailedAndReturn200() {
        GatewayAccountFixture testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
        PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                .withGatewayAccountId(testGatewayAccount.getId())
                .insert(testContext.getJdbi());
        TransactionFixture transactionFixture = aTransactionFixture()
                .withPaymentRequestId(paymentRequestFixture.getId())
                .withPaymentRequestExternalId(paymentRequestFixture.getExternalId())
                .withState(PaymentState.PENDING_DIRECT_DEBIT_PAYMENT)
                .insert(testContext.getJdbi());
        PayerFixture payerFixture = PayerFixture.aPayerFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi());
        MandateFixture mandateFixture = MandateFixture.aMandateFixture().withPayerId(payerFixture.getId()).insert(testContext.getJdbi());
        aGoCardlessPaymentFixture()
                .withPaymentId("PM00008Q30R2BR")
                .withTransactionId(transactionFixture.getId())
                .insert(testContext.getJdbi());
        aGoCardlessMandateFixture()
                .withGoCardlessMandateId("MD00008Q30R2BR")
                .withMandateId(mandateFixture.getId())
                .insert(testContext.getJdbi());

        String emailPayloadBody = "{\"address\": \"" + payerFixture.getEmail() + "\", " +
                "\"gateway_account_external_id\": \"" + testGatewayAccount.getExternalId() + "\"," +
                "\"template\": \"MANDATE_FAILED\"," +
                "\"personalisation\": " +
                    "{" +
                    "\"mandate reference\": \"" + mandateFixture.getReference() + "\", " +
                    "\"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"" +
                    "}" +
                "}";

        stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200))
        );


        given().port(testContext.getPort())
                .body(WEBHOOK_FAILED)
                .header("Webhook-Signature", WEBHOOK_FAILED_SIGNATURE)
                .accept(APPLICATION_JSON)
                .post(REQUEST_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        List<Map<String, Object>> events = testContext.getDatabaseTestHelper().getAllGoCardlessEvents();

        Map<String, Object> firstEvent = events.get(0);
        assertThat(firstEvent.get("event_id"), is("EV0000ED6V59V1"));
        assertThat(firstEvent.get("resource_type"), is("PAYMENTS"));
        assertThat(firstEvent.get("action"), is("status_unhandled_in_our_side"));

        Map<String, Object> secondEvent = events.get(1);
        assertThat(secondEvent.get("event_id"), is("EV0000ED6WBEQ0"));
        assertThat(secondEvent.get("resource_type"), is("MANDATES"));
        assertThat(secondEvent.get("action"), is("failed"));

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        MatcherAssert.assertThat(transaction.get("state"), is("FAILED"));
    }
}
