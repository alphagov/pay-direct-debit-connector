package uk.gov.pay.directdebit.webhook.gocardless.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Rule;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.junit.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
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

public class WebhookGoCardlessResourceOutOfOrderPaymentEventsIT {

    private static final GoCardlessOrganisationId GOCARDLESS_ORGANISATION_ID = GoCardlessOrganisationId.valueOf("OR123");
    private static final GoCardlessPaymentId GOCARDLESS_PAYMENT_ID = GoCardlessPaymentId.valueOf("PM123");

    private static final Map<String, Object> PAYMENT_PAID_OUT_FAILED_OUT_OF_ORDER_WEBHOOK = Map.of(
            "events", List.of(
                    Map.of(
                            "id", "EV456",
                            "created_at", "2019-07-01T16:00:00.000Z",
                            "resource_type", "payments",
                            "action", "failed",
                            "details", Map.of(
                                    "origin", "bank",
                                    "cause", "bank_account_closed",
                                    "description", "The bank account for this mandate has been closed.",
                                    "scheme", "bacs",
                                    "reason_code", "ARUDD-B"
                            ),
                            "metadata", Collections.emptyMap(),
                            "links", Map.of(
                                    "payment", GOCARDLESS_PAYMENT_ID.toString(),
                                    "organisation", GOCARDLESS_ORGANISATION_ID.toString()
                            )
                    ),
                    Map.of(
                            "id", "EV123",
                            "created_at", "2019-07-01T13:00:00.000Z",
                            "resource_type", "payments",
                            "action", "paid_out",
                            "details", Map.of(
                                    "origin", "gocardless",
                                    "cause", "payment_paid_out",
                                    "description", "Got some dough!"
                            ),
                            "metadata", Collections.emptyMap(),
                            "links", Map.of(
                                    "payment", GOCARDLESS_PAYMENT_ID.toString(),
                                    "organisation", GOCARDLESS_ORGANISATION_ID.toString()
                            )
                    )
            )
    );

    private TestContext testContext;

    private GoCardlessWebhookSignatureCalculator goCardlessWebhookSignatureCalculator;

    private ObjectMapper objectMapper = new ObjectMapper().configure(ORDER_MAP_ENTRIES_BY_KEYS, true);

    @Rule
    public DropwizardAppWithPostgresRule app = new DropwizardAppWithPostgresRule();

    @Before
    public void setUp() {
        testContext = app.getTestContext();
        goCardlessWebhookSignatureCalculator = new GoCardlessWebhookSignatureCalculator(testContext.getGoCardlessWebhookSecret());
    }

    @After
    public void tearDown() {
        app.getDatabaseTestHelper().truncateAllData();
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

        PaymentFixture paymentFixture = PaymentFixture.aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withPaymentProviderId(GOCARDLESS_PAYMENT_ID)
                .insert(testContext.getJdbi());

        String json = objectMapper.writeValueAsString(PAYMENT_PAID_OUT_FAILED_OUT_OF_ORDER_WEBHOOK);

        given().port(testContext.getPort())
                .body(json)
                .header("Webhook-Signature", goCardlessWebhookSignatureCalculator.calculate(json))
                .accept(APPLICATION_JSON)
                .post("/v1/webhooks/gocardless")
                .then()
                .statusCode(OK.getStatusCode());

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        assertThat(payment.get("state"), is("FAILED"));
    }

}
