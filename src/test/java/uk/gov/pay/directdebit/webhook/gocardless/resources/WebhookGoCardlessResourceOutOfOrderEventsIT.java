package uk.gov.pay.directdebit.webhook.gocardless.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
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
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class WebhookGoCardlessResourceOutOfOrderEventsIT {

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
    
    private static final String MANDATE_SUBMITTED_CREATED_CANCELLED_WEBHOOK_SIGNATURE = "94fa55af251148b1b99f08cd2e62110eea266a8765a6ba2a3d64438eeb91811f";

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

    private static final String MANDATE_ACTIVE_WEBHOOK_SIGNATURE = "c99b67b65c4dd02706e10079d79930af080ca00f7b268ceae3e1cdb29c6fbb90";

    @DropwizardTestContext
    private TestContext testContext;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(10111);

    private ObjectMapper objectMapper = new ObjectMapper().configure(ORDER_MAP_ENTRIES_BY_KEYS, true);

    @Test
    public void handlesOutOfOrderWebhooksByUsingLatestTimestampedEvent() throws JsonProcessingException {
        GatewayAccountFixture gatewayAccount = GatewayAccountFixture.aGatewayAccountFixture()
                .withPaymentProvider(PaymentProvider.GOCARDLESS)
                .withOrganisation(GOCARDLESS_ORGANISATION_ID)
                .insert(testContext.getJdbi());

        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(gatewayAccount)
                .withPaymentProviderId(GoCardlessMandateId.valueOf("MD123"))
                .insert(testContext.getJdbi());

        PayerFixture.aPayerFixture().withMandateId(mandateFixture.getId()).insert(testContext.getJdbi());

        given().port(testContext.getPort())
                .body(objectMapper.writeValueAsString(MANDATE_CREATED_SUBMITTED_CANCELLED_OUT_OF_ORDER_WEBHOOK))
                .header("Webhook-Signature", MANDATE_SUBMITTED_CREATED_CANCELLED_WEBHOOK_SIGNATURE)
                .accept(APPLICATION_JSON)
                .post("/v1/webhooks/gocardless")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        Map<String, Object> mandateAfterFirstWebhook = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandateAfterFirstWebhook.get("state"), is("CANCELLED"));

        given().port(testContext.getPort())
                .body(objectMapper.writeValueAsString(MANDATE_ACTIVE_WEBHOOK))
                .header("Webhook-Signature", MANDATE_ACTIVE_WEBHOOK_SIGNATURE)
                .accept(APPLICATION_JSON)
                .post("/v1/webhooks/gocardless")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        Map<String, Object> mandateAfterSecondWebhook = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandateAfterSecondWebhook.get("state"), is("CANCELLED"));
    }

}
