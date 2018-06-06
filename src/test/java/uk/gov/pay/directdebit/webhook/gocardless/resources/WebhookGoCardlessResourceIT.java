package uk.gov.pay.directdebit.webhook.gocardless.resources;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

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
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class WebhookGoCardlessResourceIT {

    private final static String REQUEST_PATH = "/v1/webhooks/gocardless";

    // language=JSON
    private final static String WEBHOOK_SUCCESS = "{\n" +
            "  \"events\": [\n" +
            "    {\n" +
            "      \"id\": \"EV0000ED6V59V1\",\n" +
            "      \"created_at\": \"2015-04-17T15:24:26.817Z\",\n" +
            "      \"resource_type\": \"payments\",\n" +
            "      \"action\": \"submitted\",\n" +
            "      \"links\": {\n" +
            "        \"payment\": \"PM00008Q30R2BR\"\n" +
            "      },\n" +
            "      \"details\": {\n" +
            "        \"origin\": \"gocardless\",\n" +
            "        \"cause\": \"payment_submitted\",\n" +
            "        \"description\": \"The payment has now been submitted to the banks, and cannot be cancelled. [SANDBOX TRANSITION]\"\n" +
            "      },\n" +
            "      \"metadata\": {}\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"EV0000ED6WBEQ0\",\n" +
            "      \"created_at\": \"2015-04-17T15:24:26.848Z\",\n" +
            "      \"resource_type\": \"payments\",\n" +
            "      \"action\": \"paid_out\",\n" +
            "      \"links\": {\n" +
            "        \"payment\": \"PM00008Q30R2BR\"\n" +
            "      },\n" +
            "      \"details\": {\n" +
            "        \"origin\": \"gocardless\",\n" +
            "        \"cause\": \"paid_out\",\n" +
            "        \"description\": \"Enough time has passed since the payment was submitted for the banks to return an error, so this payment is now confirmed. [SANDBOX TRANSITION]\"\n" +
            "      },\n" +
            "      \"metadata\": {}\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    private final static String WEBHOOK_SUCCESS_SIGNATURE = "2ee77401450fdd31490079c0fb0abf19bd1863f98108a67a18968e7eeac1c902";

    // language=JSON
    private final static String WEBHOOK_FAILED = "{\n" +
            "  \"events\": [\n" +
            "    {\n" +
            "      \"id\": \"EV0000ED6V59V1\",\n" +
            "      \"created_at\": \"2015-04-17T15:24:26.817Z\",\n" +
            "      \"resource_type\": \"payments\",\n" +
            "      \"action\": \"submitted\",\n" +
            "      \"links\": {\n" +
            "        \"payment\": \"PM00008Q30R2BR\"\n" +
            "      },\n" +
            "      \"details\": {\n" +
            "        \"origin\": \"gocardless\",\n" +
            "        \"cause\": \"payment_submitted\",\n" +
            "        \"description\": \"The payment has now been submitted to the banks, and cannot be cancelled. [SANDBOX TRANSITION]\"\n" +
            "      },\n" +
            "      \"metadata\": {}\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"EV0000ED6WBEQ0\",\n" +
            "      \"created_at\": \"2015-04-17T15:24:26.848Z\",\n" +
            "      \"resource_type\": \"mandates\",\n" +
            "      \"action\": \"failed\",\n" +
            "      \"links\": {\n" +
            "        \"mandate\": \"MD00008Q30R2BR\"\n" +
            "      },\n" +
            "      \"details\": {\n" +
            "        \"origin\": \"gocardless\",\n" +
            "        \"cause\": \"failed\",\n" +
            "        \"description\": \"Enough time has passed since the payment was submitted for the banks to return an error, so this payment is now confirmed. [SANDBOX TRANSITION]\"\n" +
            "      },\n" +
            "      \"metadata\": {}\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    private final static String WEBHOOK_FAILED_SIGNATURE = "f0b956da5de3d69ea7ad078e57d1f9d7c7fabe9848e079d399d84c8adb7c0a36";

    @DropwizardTestContext
    private TestContext testContext;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(10110);
    
    private GatewayAccountFixture testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture();

    @Before
    public void setUp() {
        testGatewayAccount.insert(testContext.getJdbi());
    }
    
    @Test
    public void handleWebhook_whenAPaidOutWebhookArrives_shouldInsertGoCardlessEventsUpdatePaymentToSuccessAndReturn200() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());
        TransactionFixture transactionFixture = aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.PENDING)
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
        assertThat(firstEvent.get("gocardless_event_id"), is("EV0000ED6V59V1"));
        assertThat(firstEvent.get("resource_type"), is("PAYMENTS"));
        assertThat(firstEvent.get("action"), is("submitted"));

        Map<String, Object> secondEvent = events.get(1);
        assertThat(secondEvent.get("gocardless_event_id"), is("EV0000ED6WBEQ0"));
        assertThat(secondEvent.get("resource_type"), is("PAYMENTS"));
        assertThat(secondEvent.get("action"), is("paid_out"));

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        MatcherAssert.assertThat(transaction.get("state"), is("SUCCESS"));
    }

    @Test
    public void handleWebhook_whenAMandateFailedWebhookArrives_shouldInsertGoCardlessEventsUpdatePaymentToFailedAndReturn200() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .withState(MandateState.PENDING)
                .withMandateType(MandateType.ONE_OFF)
                .insert(testContext.getJdbi());
        TransactionFixture transactionFixture = aTransactionFixture()                
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.PENDING)
                .insert(testContext.getJdbi());
        PayerFixture payerFixture = PayerFixture.aPayerFixture().withMandateId(mandateFixture.getId()).insert(testContext.getJdbi());
        aGoCardlessPaymentFixture()
                .withPaymentId("PM00008Q30R2BR")
                .withTransactionId(transactionFixture.getId())
                .insert(testContext.getJdbi());
        aGoCardlessMandateFixture()
                .withGoCardlessMandateId("MD00008Q30R2BR")
                .withMandateId(mandateFixture.getId())
                .insert(testContext.getJdbi());

        // language=JSON
        String emailPayloadBody = "{\n" +
                "  \"address\": \"" + payerFixture.getEmail() + "\",\n" +
                "  \"gateway_account_external_id\": \"" + testGatewayAccount.getExternalId() + "\",\n" +
                "  \"template\": \"MANDATE_FAILED\",\n" +
                "  \"personalisation\": {\n" +
                "    \"mandate reference\": \"" + mandateFixture.getReference() + "\",\n" +
                "    \"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"\n" +
                "  }\n" +
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
        assertThat(firstEvent.get("gocardless_event_id"), is("EV0000ED6V59V1"));
        assertThat(firstEvent.get("resource_type"), is("PAYMENTS"));
        assertThat(firstEvent.get("action"), is("submitted"));

        Map<String, Object> secondEvent = events.get(1);
        assertThat(secondEvent.get("gocardless_event_id"), is("EV0000ED6WBEQ0"));
        assertThat(secondEvent.get("resource_type"), is("MANDATES"));
        assertThat(secondEvent.get("action"), is("failed"));

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        MatcherAssert.assertThat(mandate.get("state"), is("FAILED"));
        
        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        MatcherAssert.assertThat(transaction.get("state"), is("FAILED"));
    }
}
