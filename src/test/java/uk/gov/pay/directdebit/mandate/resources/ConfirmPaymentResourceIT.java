package uk.gov.pay.directdebit.mandate.resources;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture.aGoCardlessCustomerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.payments.model.PaymentState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateCustomer;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateCustomerBankAccount;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateMandate;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreatePayment;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.ws.rs.core.Response;
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
        gatewayAccountFixture.insert(testContext.getJdbi());
    }

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private PayerFixture payerFixture = PayerFixture.aPayerFixture();

    private PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture()
            .withGatewayAccountId(gatewayAccountFixture.getId());
    private TransactionFixture transactionFixture = aTransactionFixture()
            .withPaymentRequestId(paymentRequestFixture.getId())
            .withGatewayAccountId(gatewayAccountFixture.getId())
            .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
            .withPaymentRequestDescription(paymentRequestFixture.getDescription())
            .withState(AWAITING_DIRECT_DEBIT_DETAILS);

    @Test
    public void confirm_shouldCreateAMandateAndUpdateCharge() {
        paymentRequestFixture.withPayerFixture(payerFixture).insert(testContext.getJdbi());
        transactionFixture.insert(testContext.getJdbi());
        String paymentRequestExternalId = paymentRequestFixture.getExternalId();
        
        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length()-2);
        String chargeDate = LocalDate.now().plusDays(4).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String emailPayloadBody = "{\"address\": \"" + payerFixture.getEmail() + "\", " +
                "\"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\"," +
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

        String confirmDetails = "{\"sort_code\": \"" + payerFixture.getSortCode() + "\", " +
                "\"account_number\": \"" + payerFixture.getAccountNumber() + "\"}";

        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        String requestPath = String.format("/v1/api/accounts/%s/payment-requests/%s/confirm", gatewayAccountFixture.getExternalId(), paymentRequestExternalId);
        given().port(testContext.getPort())
                .body(confirmDetails)
                .contentType(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        assertThat(transaction.get("state"), is("PENDING_DIRECT_DEBIT_PAYMENT"));
    }

    @Test
    public void confirm_shouldCreateACustomerBankAccountMandateAndUpdateCharge_ForGoCardless() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS).insert(testContext.getJdbi());
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture()
                .withPayerFixture(payerFixture)
                .withGatewayAccountId(gatewayAccountFixture.getId())
                .insert(testContext.getJdbi());
        TransactionFixture transactionFixture = aTransactionFixture()
                .withPaymentRequestId(paymentRequestFixture.getId())
                .withGatewayAccountId(gatewayAccountFixture.getId())
                .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
                .withPaymentRequestDescription(paymentRequestFixture.getDescription())
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withPaymentProvider(GOCARDLESS)
                .insert(testContext.getJdbi());
        
        String customerId = "CU000358S3A2FP";
        String customerBankAccountId = "BA0002WR3Z193A";
        GoCardlessCustomerFixture goCardlessCustomerFixture = aGoCardlessCustomerFixture().
                withCustomerId(customerId)
                .withCustomerBankAccountId(customerBankAccountId)
                .withPayerId(payerFixture.getId());
        stubCreateCustomer(paymentRequestFixture.getExternalId(), payerFixture, customerId);
        stubCreateCustomerBankAccount(paymentRequestFixture.getExternalId(), payerFixture, customerId, customerBankAccountId);
        stubCreateMandate(paymentRequestFixture.getExternalId(), goCardlessCustomerFixture);
        stubCreatePayment(paymentRequestFixture.getExternalId(), transactionFixture);

        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length()-2);
        String emailPayloadBody = "{\"address\": \"" + payerFixture.getEmail() + "\", " +
                "\"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\"," +
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


        String confirmDetails = "{\"sort_code\": \"" + payerFixture.getSortCode() + "\", " +
                "\"account_number\": \"" + payerFixture.getAccountNumber() + "\"}";

        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        String paymentRequestExternalId = paymentRequestFixture.getExternalId();
        String requestPath = String.format("/v1/api/accounts/%s/payment-requests/%s/confirm",
                gatewayAccountFixture.getExternalId(), paymentRequestExternalId);
        given().port(testContext.getPort())
                .contentType(APPLICATION_JSON)
                .body(confirmDetails)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper()
                .getTransactionById(transactionFixture.getId());
        assertThat(transaction.get("state"), is("PENDING_DIRECT_DEBIT_PAYMENT"));
    }

    @Test
    public void confirm_shouldFailWhenPayerDoesNotExist() {
        paymentRequestFixture.insert(testContext.getJdbi());
        transactionFixture.insert(testContext.getJdbi());
        String requestPath = String.format("/v1/api/accounts/%s/payment-requests/%s/confirm", gatewayAccountFixture.getExternalId(), paymentRequestFixture.getExternalId());

        String confirmDetails = "{\"sort_code\": \"123456\", " +
                "\"account_number\": \"12345678\"}";

        given().port(testContext.getPort())
                .body(confirmDetails)
                .contentType(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }
}
