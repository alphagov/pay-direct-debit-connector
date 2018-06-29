package uk.gov.pay.directdebit.mandate.resources;

import com.github.tomakehurst.wiremock.WireMockServer;
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
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture.aGoCardlessCustomerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateCustomer;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateCustomerBankAccount;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateMandate;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreatePayment;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class ConfirmMandateSetupResourceIT {

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

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();

    private PayerFixture payerFixture = PayerFixture.aPayerFixture();

    @Before
    public void setUp() {
        wireMockAdminUsers.start();
        gatewayAccountFixture.insert(testContext.getJdbi());
    }
    @Test
    public void confirm_shouldCreateAMandateAndUpdateCharge_ifMandateIsOneOff() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ONE_OFF)
                .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());

        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length()-2);
        String chargeDate = LocalDate.now().plusDays(4).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String emailPayloadBody = "{\"address\": \"" + payerFixture.getEmail() + "\", " +
                "\"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\"," +
                "\"template\": \"ONE_OFF_PAYMENT_CONFIRMED\"," +
                "\"personalisation\": " +
                "{" +
                "\"amount\": \"" + BigDecimal.valueOf(transactionFixture.getAmount(), 2).toString() + "\", " +
                "\"mandate reference\": \"" + mandateFixture.getMandateReference() + "\", " +
                "\"bank account last 2 digits\": \"******" +  lastTwoDigitsBankAccount + "\", " +
                "\"collection date\": \"" +  chargeDate + "\", " +
                "\"SUN\": \"THE-CAKE-IS-A-LIE\", " +
                "\"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"" +
                "}" +
                "}";

        String confirmDetails =
                "{\"sort_code\": \"" + payerFixture.getSortCode() + "\", " +
                        "\"account_number\": \"" + payerFixture.getAccountNumber() + "\", " +
                        "\"transaction_external_id\": \"" + transactionFixture.getExternalId() + "\"}";

        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        String requestPath = String.format("/v1/api/accounts/%s/mandates/%s/confirm", gatewayAccountFixture.getExternalId(), mandateFixture.getExternalId());
        given().port(testContext.getPort())
                .body(confirmDetails)
                .contentType(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        List<Map<String, Object>> transactionsForMandate = testContext.getDatabaseTestHelper().getTransactionsForMandate(mandateFixture.getExternalId());
        assertThat(transactionsForMandate.size(), is(1));
        assertThat(transactionsForMandate.get(0).get("state"), is("SUBMITTED"));
    }

    @Test
    public void confirm_shouldCreateAMandateWithoutTransaction_ifMandateIsOnDemand() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ON_DEMAND)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());

        //fixme not asserting on the email rn, we should do this we play the story to add email for on-demand. Consider payment vs mandate reference

//        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length()-2);
//        String chargeDate = LocalDate.now().plusDays(4).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
//        String emailPayloadBody = "{\"address\": \"" + payerFixture.getEmail() + "\", " +
//                "\"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\"," +
//                "\"template\": \"ONE_OFF_PAYMENT_CONFIRMED\"," +
//                "\"personalisation\": " +
//                "{" +
//                "\"amount\": \"" + BigDecimal.valueOf(transactionFixture.getAmount(), 2).toString() + "\", " +
//                "\"mandate reference\": \"" + mandateFixture.getMandateReference() + "\", " +
//                "\"bank account last 2 digits\": \"******" +  lastTwoDigitsBankAccount + "\", " +
//                "\"collection date\": \"" +  chargeDate + "\", " +
//                "\"SUN\": \"THE-CAKE-IS-A-LIE\", " +
//                "\"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"" +
//                "}" +
//                "}";
        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
//                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        String confirmDetails = "{\"sort_code\": \"" + payerFixture.getSortCode() + "\", " +
                "\"account_number\": \"" + payerFixture.getAccountNumber() + "\"}";

        String requestPath = String.format("/v1/api/accounts/%s/mandates/%s/confirm", gatewayAccountFixture.getExternalId(), mandateFixture.getExternalId());
        given().port(testContext.getPort())
                .body(confirmDetails)
                .contentType(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        List<Map<String, Object>> transactionsForMandate = testContext.getDatabaseTestHelper().getTransactionsForMandate(mandateFixture.getExternalId());
        assertThat(transactionsForMandate, is(empty()));
    }

    @Test
    public void confirm_shouldCreateACustomerBankAccountMandateAndUpdateCharge_ForGoCardless_ifMandateIsOneOff() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS).insert(testContext.getJdbi());
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ONE_OFF)
                .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());

        String customerId = "CU000358S3A2FP";
        String customerBankAccountId = "BA0002WR3Z193A";
        GoCardlessCustomerFixture goCardlessCustomerFixture = aGoCardlessCustomerFixture().
                withCustomerId(customerId)
                .withCustomerBankAccountId(customerBankAccountId)
                .withPayerId(payerFixture.getId());
        stubCreateCustomer(mandateFixture.getExternalId(), payerFixture, customerId);
        stubCreateCustomerBankAccount(mandateFixture.getExternalId(), payerFixture, customerId, customerBankAccountId);
        stubCreateMandate(mandateFixture.getExternalId(), goCardlessCustomerFixture);
        stubCreatePayment(transactionFixture.getAmount(), "MD123", transactionFixture.getExternalId());

        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length()-2);
        String emailPayloadBody = "{\"address\": \"" + payerFixture.getEmail() + "\", " +
                "\"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\"," +
                "\"template\": \"ONE_OFF_PAYMENT_CONFIRMED\"," +
                "\"personalisation\": " +
                "{" +
                "\"amount\": \"" + BigDecimal.valueOf(transactionFixture.getAmount(), 2).toString() + "\", " +
                "\"mandate reference\": \"" + mandateFixture.getMandateReference() + "\", " +
                "\"bank account last 2 digits\": \"******" +  lastTwoDigitsBankAccount + "\", " +
                "\"collection date\": \"2014-05-21\", " +
                "\"SUN\": \"THE-CAKE-IS-A-LIE\", " +
                "\"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"" +
                "}" +
                "}";

        String confirmDetails =
                "{\"sort_code\": \"" + payerFixture.getSortCode() + "\", " + 
                        "\"account_number\": \"" + payerFixture.getAccountNumber() + "\", " +
                        "\"transaction_external_id\": \"" + transactionFixture.getExternalId() + "\"}";

        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        String requestPath = String.format("/v1/api/accounts/%s/mandates/%s/confirm",
                gatewayAccountFixture.getExternalId(), mandateFixture.getExternalId());
        given().port(testContext.getPort())
                .contentType(APPLICATION_JSON)
                .body(confirmDetails)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        List<Map<String, Object>> transactionsForMandate = testContext.getDatabaseTestHelper().getTransactionsForMandate(mandateFixture.getExternalId());
        assertThat(transactionsForMandate.size(), is(1));
        assertThat(transactionsForMandate.get(0).get("state"), is("SUBMITTED"));
    }

    @Test
    public void confirm_shouldCreateACustomerBankAccountMandateWithNoTransaction_ForGoCardless_ifMandateIsOnDemand() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS).insert(testContext.getJdbi());
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ON_DEMAND)
                .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());

        String customerId = "CU000358S3A2FP";
        String customerBankAccountId = "BA0002WR3Z193A";
        GoCardlessCustomerFixture goCardlessCustomerFixture = aGoCardlessCustomerFixture().
                withCustomerId(customerId)
                .withCustomerBankAccountId(customerBankAccountId)
                .withPayerId(payerFixture.getId());
        stubCreateCustomer(mandateFixture.getExternalId(), payerFixture, customerId);
        stubCreateCustomerBankAccount(mandateFixture.getExternalId(), payerFixture, customerId, customerBankAccountId);
        stubCreateMandate(mandateFixture.getExternalId(), goCardlessCustomerFixture);

//        String lastTwoDigitsBankAccount = payerFixture.getAccountNumber().substring(payerFixture.getAccountNumber().length()-2);

        //fixme not asserting on the email rn, we should do this we play the story to add email for on-demand. Consider payment vs mandate reference

//        String emailPayloadBody = "{\"address\": \"" + payerFixture.getEmail() + "\", " +
//                "\"gateway_account_external_id\": \"" + gatewayAccountFixture.getExternalId() + "\"," +
//                "\"template\": \"ONE_OFF_PAYMENT_CONFIRMED\"," +
//                "\"personalisation\": " +
//                "{" +
//                "\"amount\": \"" + BigDecimal.valueOf(transactionFixture.getAmount(), 2).toString() + "\", " +
//                "\"mandate reference\": \"" + mandateFixture.getMandateReference() + "\", " +
//                "\"bank account last 2 digits\": \"******" +  lastTwoDigitsBankAccount + "\", " +
//                "\"collection date\": \"2014-05-21\", " +
//                "\"SUN\": \"THE-CAKE-IS-A-LIE\", " +
//                "\"dd guarantee link\": \"http://Frontend/direct-debit-guarantee\"" +
//                "}" +
//                "}";
        wireMockAdminUsers.stubFor(post(urlPathEqualTo("/v1/emails/send"))
//                .withRequestBody(equalToJson(emailPayloadBody))
                .willReturn(
                        aResponse().withStatus(200)));

        String confirmDetails = "{\"sort_code\": \"" + payerFixture.getSortCode() + "\", " +
                "\"account_number\": \"" + payerFixture.getAccountNumber() + "\"}";

        String requestPath = String.format("/v1/api/accounts/%s/mandates/%s/confirm",
                gatewayAccountFixture.getExternalId(), mandateFixture.getExternalId());
        given().port(testContext.getPort())
                .contentType(APPLICATION_JSON)
                .body(confirmDetails)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        List<Map<String, Object>> transactionsForMandate = testContext.getDatabaseTestHelper().getTransactionsForMandate(mandateFixture.getExternalId());
        assertThat(transactionsForMandate, is(empty()));
    }
}
