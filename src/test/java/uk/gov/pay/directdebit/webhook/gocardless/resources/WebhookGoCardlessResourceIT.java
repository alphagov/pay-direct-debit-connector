package uk.gov.pay.directdebit.webhook.gocardless.resources;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
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
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

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
            "        \"payment\": \"PM00008Q30R2BR\",\n" +
            "        \"organisation\": \"test_organisation_id\"\n" +
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
            "        \"payment\": \"PM00008Q30R2BR\",\n" +
            "        \"organisation\": \"test_organisation_id\"\n" +
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
    private final static String WEBHOOK_SUCCESS_SIGNATURE = "48da9fe6ac1ff84558d91895d1f10d971e391a43e9991440e97761e1003ffe09";

    // language=JSON
    private final static String WEBHOOK_FAILED = "{\n" +
            "  \"events\": [\n" +
            "    {\n" +
            "      \"id\": \"EV0000ED6V59V1\",\n" +
            "      \"created_at\": \"2015-04-17T15:24:26.817Z\",\n" +
            "      \"resource_type\": \"payments\",\n" +
            "      \"action\": \"submitted\",\n" +
            "      \"links\": {\n" +
            "        \"payment\": \"PM00008Q30R2BR\",\n" +
            "        \"organisation\": \"test_organisation_id\"\n" +
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
            "        \"mandate\": \"MD00008Q30R2BR\",\n" +
            "        \"organisation\": \"test_organisation_id\"\n" +
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
    private final static String WEBHOOK_FAILED_SIGNATURE = "c01e874cafffa3d4cce4b35b003be0be14b9a9c933f3b04045980afb27c54421";

    @DropwizardTestContext
    private TestContext testContext;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(10110);

    private GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_organisation_id");

    private GatewayAccountFixture testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture()
            .withPaymentProvider(PaymentProvider.GOCARDLESS)
            .withOrganisation(organisationIdentifier);

    @Before
    public void setUp() {
        testGatewayAccount.insert(testContext.getJdbi());
    }

    @Test
    public void handleWebhook_whenAPaidOutWebhookArrives_shouldInsertGoCardlessEventsUpdatePaymentToSuccessAndReturn200() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());
        PaymentFixture paymentFixture = aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withPaymentProviderId(GoCardlessPaymentId.valueOf("PM00008Q30R2BR"))
                .withState(PaymentState.PENDING)
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
        assertThat(firstEvent.get("action"), is("submitted"));

        Map<String, Object> secondEvent = events.get(1);
        assertThat(secondEvent.get("event_id"), is("EV0000ED6WBEQ0"));
        assertThat(secondEvent.get("resource_type"), is("PAYMENTS"));
        assertThat(secondEvent.get("action"), is("paid_out"));

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        MatcherAssert.assertThat(transaction.get("state"), is("SUCCESS"));
    }

    @Test
    public void handleWebhook_whenAMandateFailedWebhookArrives_shouldInsertGoCardlessEventsUpdateMandateToFailedAndReturn200() {

        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .withState(MandateState.PENDING)
                .withPaymentProviderId(GoCardlessMandateId.valueOf("MD00008Q30R2BR"))
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withPaymentProviderId(GoCardlessPaymentId.valueOf("PM00008Q30R2BR"))
                .withState(PaymentState.PENDING)
                .insert(testContext.getJdbi());

        PayerFixture payerFixture = PayerFixture.aPayerFixture()
                .withMandateId(mandateFixture.getId())
                .insert(testContext.getJdbi());

        // language=JSON
        String emailPayloadBody = "{\n" +
                "  \"address\": \"" + payerFixture.getEmail() + "\",\n" +
                "  \"gateway_account_external_id\": \"" + testGatewayAccount.getExternalId() + "\",\n" +
                "  \"template\": \"MANDATE_FAILED\",\n" +
                "  \"personalisation\": {\n" +
                "    \"mandate reference\": \"" + mandateFixture.getMandateReference() + "\",\n" +
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
        assertThat(firstEvent.get("event_id"), is("EV0000ED6V59V1"));
        assertThat(firstEvent.get("resource_type"), is("PAYMENTS"));
        assertThat(firstEvent.get("action"), is("submitted"));

        Map<String, Object> secondEvent = events.get(1);
        assertThat(secondEvent.get("event_id"), is("EV0000ED6WBEQ0"));
        assertThat(secondEvent.get("resource_type"), is("MANDATES"));
        assertThat(secondEvent.get("action"), is("failed"));

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        MatcherAssert.assertThat(mandate.get("state"), is("FAILED"));
    }

}
