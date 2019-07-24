package uk.gov.pay.directdebit.webhook.gocardless.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Ignore;
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
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.google.common.base.Charsets.UTF_8;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class WebhookGoCardlessResourceMandateActionsIT {

    @Rule
    public WireMockRule wireMockRuleForAdminUsersPort = new WireMockRule(10110);

    private final GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
            .withPaymentProvider(PaymentProvider.GOCARDLESS)
            .withOrganisation(GoCardlessOrganisationId.valueOf("OR00003V8M32F0"));
    
    private final MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withGatewayAccountFixture(gatewayAccountFixture)
            .withPaymentProviderId(GoCardlessMandateId.valueOf("MD0006APPY4N63"));

    private final PayerFixture payerFixture = PayerFixture.aPayerFixture()
            .withMandateId(mandateFixture.getId());

    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setUp() {
        gatewayAccountFixture.insert(testContext.getJdbi());
        mandateFixture.insert(testContext.getJdbi());
        payerFixture.insert(testContext.getJdbi());
    }

    @Test
    @Ignore
    public void submittedChangesStateToSubmittedToBank() throws IOException {
        postWebhook("gocardless-webhook-mandate-submitted.json",
                "d661429adff81d7a7b584eb8f91fece6ee642002a7295db3289eb7fa5da6b750");

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandate.get("state"), is("SUBMITTED_TO_BANK"));
        assertThat(mandate.get("state_details"), is("mandate_submitted"));
        assertThat(mandate.get("state_details_description"), is("The mandate has been submitted to the banks."));
    }

    @Test
    public void activeChangesStateToActive() throws IOException {
        postWebhook("gocardless-webhook-mandate-active.json",
                "c6ce7e71e092661519d161f5798d5f2730949e1338a59eaaec774c769154ae6e");

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandate.get("state"), is(MandateState.ACTIVE.toString()));
        assertThat(mandate.get("state_details"), is("mandate_activated"));
        assertThat(mandate.get("state_details_description"), is("The time window after submission for the banks to refuse a mandate has ended " +
                "without any errors being received, so this mandate is now active."));
    }

    @Test
    @Ignore
    public void reinstatedChangesStateToActive() throws IOException {
        postWebhook("gocardless-webhook-mandate-reinstated.json",
                "3c6d041f03bc2b591addf64f8e5933626eacb3f765b4e444d5d78e2f7b084526");

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandate.get("state"), is(MandateState.ACTIVE.toString()));
        assertThat(mandate.get("state_details"), is("mandate_reinstated"));
        assertThat(mandate.get("state_details_description"), is("A cancelled mandate has been re-instated by the customer's bank."));
    }

    @Test
    public void cancelledChangesStateToCancelledAndSendsMandateCancelledEmail() throws IOException {
        stubEmail(EmailTemplate.MANDATE_CANCELLED);

        postWebhook("gocardless-webhook-mandate-cancelled.json",
                "b019901d05cedd31ca878b33789010cbd012f579b079af760cf03c275eeb6b13");

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandate.get("state"), is(MandateState.CANCELLED.toString()));
        assertThat(mandate.get("state_details"), is("mandate_cancelled"));
        assertThat(mandate.get("state_details_description"), is("The mandate was cancelled at a bank branch."));

        verifyEmailSent(EmailTemplate.MANDATE_CANCELLED);
    }

    @Test
    public void failedChangesStateToFailedAndSendsMandateFailedEmail() throws IOException {
        stubEmail(EmailTemplate.MANDATE_FAILED);

        postWebhook("gocardless-webhook-mandate-failed.json",
                "1978e4564c85b026146649b149d6ebb20e3395d5213b2eba72fd32da98ecfff2");

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandate.get("state"), is(MandateState.FAILED.toString()));
        assertThat(mandate.get("state_details"), is("invalid_bank_details"));
        assertThat(mandate.get("state_details_description"), is("The specified bank account does not exist or was closed."));

        verifyEmailSent(EmailTemplate.MANDATE_FAILED);
    }

    @Test
    @Ignore
    public void expiredChangesStateToFailed() throws IOException {
        postWebhook("gocardless-webhook-mandate-expired.json",
                "492f08e710849470e7103212b8ee4256dc193fa57561488ed6640435684bab39");

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandate.get("state"), is(MandateState.EXPIRED.toString()));
        assertThat(mandate.get("state_details"), is("mandate_expired"));
        assertThat(mandate.get("state_details_description"), is("The mandate is being marked as expired, because no payments have been collected " +
                "against it for the dormancy period of your service user number. If you have access to the mandate reinstation API endpoint, you can use " +
                "this to attempt to set this mandate up again."));
    }

    private void postWebhook(String webhookBodyResourceName, String webhookSignature) throws IOException {
        given().port(testContext.getPort())
                .body(Resources.toString(Resources.getResource(webhookBodyResourceName), UTF_8))
                .header("Webhook-Signature", webhookSignature)
                .accept(APPLICATION_JSON)
                .post("/v1/webhooks/gocardless")
                .then()
                .statusCode(OK.getStatusCode());
    }

    private void stubEmail(EmailTemplate template) throws JsonProcessingException {
        stubFor(post(urlEqualTo("/v1/emails/send")).withRequestBody(equalToJson(emailPayload(template)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())));
    }

    private void verifyEmailSent(EmailTemplate template) throws JsonProcessingException {
        verify(postRequestedFor(urlEqualTo("/v1/emails/send")).withRequestBody(equalToJson(emailPayload(template))));
    }

    private String emailPayload(EmailTemplate template) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(Map.of(
                "address", payerFixture.getEmail(),
                "gateway_account_external_id", gatewayAccountFixture.getExternalId(),
                "template", template.toString(),
                "personalisation", Map.of(
                        "mandate reference", mandateFixture.getMandateReference().toString(),
                        "dd guarantee link", "http://Frontend/direct-debit-guarantee"
                )
        ));
    }

}
