package uk.gov.pay.directdebit.webhook.gocardless.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
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
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.webhook.gocardless.support.GoCardlessWebhookSignatureCalculator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class WebhookGoCardlessResourceOutOfOrderMandateEventsIT {

    private static final GoCardlessOrganisationId GOCARDLESS_ORGANISATION_ID = GoCardlessOrganisationId.valueOf("OR123");
    private static final GoCardlessMandateId GOCARDLESS_MANDATE_ID = GoCardlessMandateId.valueOf("MD123");
    
    private static final Map<String, Object> MANDATE_CREATED_SUBMITTED_CANCELLED_OUT_OF_ORDER_WEBHOOK = Map.of(
            "events", List.of(
                    Map.of(
                            "id", "EV123",
                            "created_at", "2019-07-01T13:00:00.000Z",
                            "resource_type", "mandates",
                            "action", "cancelled",
                            "details", Map.of(
                                    "origin", "api",
                                    "cause", "mandate_created",
                                    "description", "Shiny new mandate!"
                            ),
                            "metadata", Collections.emptyMap(),
                            "links", Map.of(
                                    "mandate", GOCARDLESS_MANDATE_ID.toString(),
                                    "organisation", GOCARDLESS_ORGANISATION_ID.toString()
                            )
                    ),
                    Map.of(
                            "id", "EV456",
                            "created_at", "2019-07-01T16:00:00.000Z",
                            "resource_type", "mandates",
                            "action", "cancelled",
                            "details", Map.of(
                                    "origin", "bank",
                                    "cause", "bank_account_closed",
                                    "description", "The bank account for this mandate has been closed.",
                                    "scheme", "bacs",
                                    "reason_code", "ADDACS-B"
                            ),
                            "metadata", Collections.emptyMap(),
                            "links", Map.of(
                                    "mandate", GOCARDLESS_MANDATE_ID.toString(),
                                    "organisation", GOCARDLESS_ORGANISATION_ID.toString()
                            )
                    ),
                    Map.of(
                            "id", "EV789",
                            "created_at", "2019-07-01T14:00:00.000Z",
                            "resource_type", "mandates",
                            "action", "submitted",
                            "details", Map.of(
                                    "origin", "gocardless",
                                    "cause", "mandate_submitted",
                                    "description", "Note this actually happened before the cancelled one."
                            ),
                            "metadata", Collections.emptyMap(),
                            "links", Map.of(
                                    "mandate", GOCARDLESS_MANDATE_ID.toString(),
                                    "organisation", GOCARDLESS_ORGANISATION_ID.toString()
                            )
                    )
            )
    );

    private static final Map<String, Object> MANDATE_ACTIVE_WEBHOOK = Map.of(
            "events", List.of(
                    Map.of(
                            "id", "EV012",
                            "created_at", "2019-07-01T15:00:00.000Z",
                            "resource_type", "mandates",
                            "action", "active",
                            "details", Map.of(
                                    "origin", "gocardless",
                                    "cause", "mandate_activated",
                                    "description", "Note this actually happened before the cancelled one but after the created one."
                            ),
                            "metadata", Collections.emptyMap(),
                            "links", Map.of(
                                    "mandate", GOCARDLESS_MANDATE_ID.toString(),
                                    "organisation", GOCARDLESS_ORGANISATION_ID.toString()
                            )
                    )
            )
    );

    @DropwizardTestContext
    private TestContext testContext;

    private GoCardlessWebhookSignatureCalculator goCardlessWebhookSignatureCalculator;

    private ObjectMapper objectMapper = new ObjectMapper().configure(ORDER_MAP_ENTRIES_BY_KEYS, true);

    @Before
    public void setUp() {
        goCardlessWebhookSignatureCalculator = new GoCardlessWebhookSignatureCalculator(testContext.getGoCardlessWebhookSecret());
    }

    @Test
    public void handlesOutOfOrderWebhooksByUsingLatestTimestampedEvent() throws IOException {
        GatewayAccountFixture gatewayAccount = GatewayAccountFixture.aGatewayAccountFixture()
                .withPaymentProvider(PaymentProvider.GOCARDLESS)
                .withOrganisation(GOCARDLESS_ORGANISATION_ID)
                .insert(testContext.getJdbi());

        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccount)
                .withPaymentProviderId(GoCardlessMandateId.valueOf("MD123"))
                .insert(testContext.getJdbi());

        PayerFixture.aPayerFixture().withMandateId(mandateFixture.getId()).insert(testContext.getJdbi());

        postWebhook(MANDATE_CREATED_SUBMITTED_CANCELLED_OUT_OF_ORDER_WEBHOOK);

        Map<String, Object> mandateAfterFirstWebhook = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandateAfterFirstWebhook.get("state"), is("USER_SETUP_CANCELLED"));
 
        postWebhook(MANDATE_ACTIVE_WEBHOOK);

        Map<String, Object> mandateAfterSecondWebhook = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandateAfterSecondWebhook.get("state"), is("USER_SETUP_CANCELLED"));
    }

    private void postWebhook(Map<String, Object> webhook) throws IOException {
        String json = objectMapper.writeValueAsString(webhook);
        given().port(testContext.getPort())
                .body(json)
                .header("Webhook-Signature", goCardlessWebhookSignatureCalculator.calculate(json))
                .accept(APPLICATION_JSON)
                .post("/v1/webhooks/gocardless")
                .then()
                .statusCode(OK.getStatusCode());
    }

}
