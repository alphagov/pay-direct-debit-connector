package uk.gov.pay.directdebit.mandate.resources;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture.aGoCardlessCustomerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.payments.model.PaymentState.AWAITING_CONFIRMATION;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateMandate;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreatePayment;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class ConfirmPaymentResourceIT {

    @DropwizardTestContext
    private TestContext testContext;

    //todo we should be able to override this in the test-it-config or else tests won't easily run in parallel. See https://payments-platform.atlassian.net/browse/PP-3374
    @Rule
    public WireMockRule wireMockRuleGoCardless = new WireMockRule(10107);

    private WireMockServer wireMockAdminUsers = new WireMockServer(options().port(10110));

    @After
    public void tearDown() {
        wireMockAdminUsers.shutdown();
    }

    @Before
    public void setUp() {
        wireMockAdminUsers.start();
    }

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture()
            .withGatewayAccountId(gatewayAccountFixture.getId());
    private TransactionFixture transactionFixture = aTransactionFixture()
            .withPaymentRequestId(paymentRequestFixture.getId())
            .withGatewayAccountId(gatewayAccountFixture.getId())
            .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
            .withPaymentRequestDescription(paymentRequestFixture.getDescription())
            .withState(AWAITING_CONFIRMATION);
    @Test
    public void confirm_shouldCreateAMandateAndUpdateCharge() {
        gatewayAccountFixture.insert(testContext.getJdbi());
        paymentRequestFixture.insert(testContext.getJdbi());
        transactionFixture.insert(testContext.getJdbi());
        String paymentRequestExternalId = paymentRequestFixture.getExternalId();
        PayerFixture payerFixture = PayerFixture.aPayerFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi());

        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length()-2);
        String chargeDate = LocalDate.now().plusDays(4).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String emailPayloadBody = "{\"address\": \"" + payerFixture.getEmail() + "\", " +
                "\"gateway_account_id\": \"" + gatewayAccountFixture.getExternalId() + "\"," +
                "\"template\": \"PAYMENT_CONFIRMED\"," +
                "\"personalisation\": " +
                "{" +
                "\"amount\": \"" + BigDecimal.valueOf(transactionFixture.getAmount(), 2).toString() + "\", " +
                "\"payment reference\": \"" + paymentRequestFixture.getReference() + "\", " +
                "\"bank account last 2 digits\": \"******" +  lastTwoDigitsBankAccount + "\", " +
                "\"collection date\": \"" +  chargeDate + "\", " +
                "\"SUN\": \"THE-CAKE-IS-A-LIE\", " +
                "\"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"" +
                "}" +
                "}";


        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        String requestPath = String.format("/v1/api/accounts/%s/payment-requests/%s/confirm", gatewayAccountFixture.getId().toString(), paymentRequestExternalId);
        given().port(testContext.getPort())
                .accept(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        assertThat(transaction.get("state"), is("PENDING_DIRECT_DEBIT_PAYMENT"));
    }

    @Test
    public void confirm_shouldCreateAMandateAndUpdateCharge_ForGoCardless() {
        gatewayAccountFixture.withPaymentProvider(GOCARDLESS).insert(testContext.getJdbi());
        paymentRequestFixture.insert(testContext.getJdbi());
        PayerFixture payerFixture = PayerFixture.aPayerFixture()
                .withPaymentRequestId(paymentRequestFixture.getId())
                .insert(testContext.getJdbi());
        GoCardlessCustomerFixture goCardlessCustomerFixture = aGoCardlessCustomerFixture().
                withCustomerId("CU3498578")
                .withPayerId(payerFixture.getId())
                .insert(testContext.getJdbi());
        stubCreateMandate(paymentRequestFixture.getExternalId(), goCardlessCustomerFixture);
        stubCreatePayment(paymentRequestFixture.getExternalId(), transactionFixture);

        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length()-2);
        String emailPayloadBody = "{\"address\": \"" + payerFixture.getEmail() + "\", " +
                "\"gateway_account_id\": \"" + gatewayAccountFixture.getExternalId() + "\"," +
                "\"template\": \"PAYMENT_CONFIRMED\"," +
                "\"personalisation\": " +
                "{" +
                "\"amount\": \"" + BigDecimal.valueOf(transactionFixture.getAmount(), 2).toString() + "\", " +
                "\"payment reference\": \"" + paymentRequestFixture.getReference() + "\", " +
                "\"bank account last 2 digits\": \"******" +  lastTwoDigitsBankAccount + "\", " +
                "\"collection date\": \"2014-05-21\", " +
                "\"SUN\": \"THE-CAKE-IS-A-LIE\", " +
                "\"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"" +
                "}" +
                "}";


        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        transactionFixture.withPaymentProvider(GOCARDLESS).insert(testContext.getJdbi());
        String paymentRequestExternalId = paymentRequestFixture.getExternalId();
        PayerFixture.aPayerFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi());

        String requestPath = String.format("/v1/api/accounts/%s/payment-requests/%s/confirm", gatewayAccountFixture.getId().toString(), paymentRequestExternalId);
        given().port(testContext.getPort())
                .accept(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        assertThat(transaction.get("state"), is("PENDING_DIRECT_DEBIT_PAYMENT"));
    }

    @Test
    public void confirm_shouldFailWhenPayerDoesNotExist() {
        gatewayAccountFixture.insert(testContext.getJdbi());
        paymentRequestFixture.insert(testContext.getJdbi());
        transactionFixture.insert(testContext.getJdbi());

        String requestPath = String.format("/v1/api/accounts/%s/payment-requests/%s/confirm", gatewayAccountFixture.getId().toString(), paymentRequestFixture.getExternalId());
        given().port(testContext.getPort())
                .accept(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }
}
