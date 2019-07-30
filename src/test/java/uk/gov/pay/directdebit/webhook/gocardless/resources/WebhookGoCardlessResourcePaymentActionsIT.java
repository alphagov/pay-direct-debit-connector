package uk.gov.pay.directdebit.webhook.gocardless.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
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
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.webhook.gocardless.support.GoCardlessWebhookSignatureCalculator;

import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class WebhookGoCardlessResourcePaymentActionsIT {

    @Rule
    public WireMockRule wireMockRuleForAdminUsersPort = new WireMockRule(10110);

    @DropwizardTestContext
    private TestContext testContext;

    private GoCardlessWebhookSignatureCalculator goCardlessWebhookSignatureCalculator;

    private final GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
            .withPaymentProvider(PaymentProvider.GOCARDLESS)
            .withOrganisation(GoCardlessOrganisationId.valueOf("OR00003V8M32F0"));

    private final MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withGatewayAccountFixture(gatewayAccountFixture);

    private final PayerFixture payerFixture = PayerFixture.aPayerFixture()
            .withMandateId(mandateFixture.getId());

    private final PaymentFixture paymentFixture = PaymentFixture.aPaymentFixture()
            .withMandateFixture(mandateFixture)
            .withPaymentProviderId(GoCardlessPaymentId.valueOf("PM000JWCBM6ABD"));

    @Before
    public void setUp() {
        goCardlessWebhookSignatureCalculator = new GoCardlessWebhookSignatureCalculator(testContext.getGoCardlessWebhookSecret());

        gatewayAccountFixture.insert(testContext.getJdbi());
        mandateFixture.insert(testContext.getJdbi());
        payerFixture.insert(testContext.getJdbi());
        paymentFixture.insert(testContext.getJdbi());
    }

    @Test
    public void customerApprovalDeniedChangesStateToCustomerApprovalDenied() {
        postWebhook("gocardless-webhook-payment-customer-approval-denied.json");

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        assertThat(payment.get("state"), is(PaymentState.CUSTOMER_APPROVAL_DENIED.toString()));
        assertThat(payment.get("state_details"), is("customer_approval_denied"));
        assertThat(payment.get("state_details_description"), is("The customer denied approval for this payment"));
    }

    @Test
    public void submittedChangesStateToSubmittedToBank() {
        postWebhook("gocardless-webhook-payment-submitted.json");

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        assertThat(payment.get("state"), is(PaymentState.SUBMITTED_TO_BANK.toString()));
        assertThat(payment.get("state_details"), is("payment_submitted"));
        assertThat(payment.get("state_details_description"), is("Payment submitted to the banks. As a result, it can no longer be cancelled."));
    }

    @Test
    public void confirmedChangesStateToCollectedByProvider() {
        postWebhook("gocardless-webhook-payment-confirmed.json");

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        assertThat(payment.get("state"), is(PaymentState.COLLECTED_BY_PROVIDER.toString()));
        assertThat(payment.get("state_details"), is("payment_confirmed"));
        assertThat(payment.get("state_details_description"), is("Enough time has passed since the payment was submitted for the banks to return an " +
                "error, so this payment is now confirmed."));
    }

    @Test
    public void cancelledChangesStateToCancelled() {
        postWebhook("gocardless-webhook-payment-cancelled.json");

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        assertThat(payment.get("state"), is(PaymentState.CANCELLED.toString()));
        assertThat(payment.get("state_details"), is("mandate_cancelled"));
        assertThat(payment.get("state_details_description"), is("The mandate for this payment was cancelled at a bank branch."));
    }

    @Test
    public void failedChangesStateToFailedAndSendsPaymentFailedEmail() throws IOException {
        stubEmail(EmailTemplate.PAYMENT_FAILED);

        postWebhook("gocardless-webhook-payment-failed.json");

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        assertThat(payment.get("state"), is(PaymentState.FAILED.toString()));
        assertThat(payment.get("state_details"), is("bank_account_closed"));
        assertThat(payment.get("state_details_description"), is("This payment failed because the customer is deceased."));

        verifyEmailSent(EmailTemplate.PAYMENT_FAILED);
    }

    @Test
    public void chargedBackChangesStateToIndemnityClaim() {
        postWebhook("gocardless-webhook-payment-charged-back.json");

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        assertThat(payment.get("state"), is(PaymentState.INDEMNITY_CLAIM.toString()));
        assertThat(payment.get("state_details"), is("authorisation_disputed"));
        assertThat(payment.get("state_details_description"), is("The customer has disputed that the amount taken differs from the amount they were " +
                "notified of."));
    }

    @Test
    public void chargebackCancelledChangesStateToPaidOut() {
        postWebhook("gocardless-webhook-payment-chargeback-cancelled.json");

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        assertThat(payment.get("state"), is(PaymentState.PAID_OUT.toString()));
        assertThat(payment.get("state_details"), is("payment_confirmed"));
        assertThat(payment.get("state_details_description"), is("The chargeback for this payment was reversed"));
    }

    @Test
    public void lateFailureSettledChangesStateToFailed() {
        postWebhook("gocardless-webhook-payment-late-failure-settled.json");

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        assertThat(payment.get("state"), is(PaymentState.FAILED.toString()));
        assertThat(payment.get("state_details"), is("late_failure_settled"));
        assertThat(payment.get("state_details_description"), is("This late failed payment has been settled against a payout."));
    }

    @Test
    public void paidOutChangesStateToPaidOut() {
        postWebhook("gocardless-webhook-payment-paid-out.json");

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        assertThat(payment.get("state"), is(PaymentState.PAID_OUT.toString()));
        assertThat(payment.get("state_details"), is("payment_paid_out"));
        assertThat(payment.get("state_details_description"), is("The payment has been paid out by GoCardless."));
    }

    @Test
    public void chargebackSettledChangesStateToIndemnityClaim() {
        postWebhook("gocardless-webhook-payment-chargeback-settled.json");

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(paymentFixture.getId());
        assertThat(payment.get("state"), is(PaymentState.INDEMNITY_CLAIM.toString()));
        assertThat(payment.get("state_details"), is("chargeback_settled"));
        assertThat(payment.get("state_details_description"), is("This charged back payment has been settled against a payout."));
    }

    private void postWebhook(String webhookBodyFileName) {
        String body = fixture(webhookBodyFileName);
        given().port(testContext.getPort())
                .body(body)
                .header("Webhook-Signature", goCardlessWebhookSignatureCalculator.calculate(body))
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
                        "dd guarantee link", "http://Frontend/direct-debit-guarantee"
                )
        ));
    }

}
