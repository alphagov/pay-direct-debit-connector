package uk.gov.pay.directdebit.payers.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
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
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateCustomer;
import static uk.gov.pay.directdebit.util.GoCardlessStubs.stubCreateCustomerBankAccount;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PayerResourceIT {
    private final static String ACCOUNT_NUMBER_KEY = "account_number";
    private final static String SORTCODE_KEY = "sort_code";
    private final static String NAME_KEY = "account_holder_name";
    private final static String EMAIL_KEY = "email";
    private final static String ADDRESS_LINE1_KEY = "address_line1";
    private final static String ADDRESS_LINE2_KEY = "address_line2";
    private final static String ADDRESS_CITY_KEY = "city";
    private final static String ADDRESS_COUNTRY_KEY = "country_code";
    private final static String ADDRESS_POSTCODE_KEY = "postcode";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @DropwizardTestContext
    private TestContext testContext;

    //todo we should be able to override this in the test-it-config or else tests won't easily run in parallel. See https://payments-platform.atlassian.net/browse/PP-3374
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(10107);

    private GatewayAccountFixture testGatewayAccount;
    private PaymentRequestFixture testPaymentRequest;
    private PayerFixture payerFixture;

    String requestPath;

    @Before
    public void setUp() {
        testGatewayAccount = aGatewayAccountFixture().insert(testContext.getJdbi());
        testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(testGatewayAccount.getId())
                .insert(testContext.getJdbi());
        payerFixture = aPayerFixture().withAccountNumber("12345678");
        requestPath = "/v1/api/accounts/{accountId}/payment-requests/{paymentRequestExternalId}/payers"
                .replace("{accountId}", testGatewayAccount.getId().toString())
                .replace("{paymentRequestExternalId}", testPaymentRequest.getExternalId());
    }

    @After
    public void tearDown() {
        wireMockRule.shutdown();
    }

    private TransactionFixture insertTransactionFixtureWith(PaymentProvider paymentProvider) {
        return  aTransactionFixture()
                .withState(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withPaymentRequestId(testPaymentRequest.getId())
                .withPaymentRequestGatewayAccountId(testGatewayAccount.getId())
                .withPaymentProvider(paymentProvider)
                .withPaymentRequestExternalId(testPaymentRequest.getExternalId())
                .insert(testContext.getJdbi());
    }

    @Test
    public void shouldCreateAPayer() throws JsonProcessingException {
        insertTransactionFixtureWith(SANDBOX);
        String postBody = OBJECT_MAPPER.writeValueAsString(ImmutableMap.builder()
                .put(ACCOUNT_NUMBER_KEY, payerFixture.getAccountNumber())
                .put(SORTCODE_KEY, payerFixture.getSortCode())
                .put(NAME_KEY, payerFixture.getName())
                .put(EMAIL_KEY, payerFixture.getEmail())
                .put(ADDRESS_LINE1_KEY, payerFixture.getAddressLine1())
                .put(ADDRESS_CITY_KEY, payerFixture.getAddressCity())
                .put(ADDRESS_COUNTRY_KEY, payerFixture.getAddressCountry())
                .put(ADDRESS_POSTCODE_KEY, payerFixture.getAddressPostcode())
                .build());

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());

        Map<String, Object> createdPayer = testContext.getDatabaseTestHelper().getPayerByPaymentRequestExternalId(testPaymentRequest.getExternalId());
        String createdPayerExternalId = (String) createdPayer.get("external_id");
        String documentLocation = expectedPayerRequestLocationFor(testPaymentRequest.getExternalId(), createdPayerExternalId);

        response
                .header("Location", is(documentLocation))
                .body("payer_external_id", is(createdPayerExternalId))
                .contentType(JSON);
    }

    @Test
    public void shouldCreateAPayer_withoutAddress() throws JsonProcessingException {
        insertTransactionFixtureWith(SANDBOX);
        String postBody = OBJECT_MAPPER.writeValueAsString(ImmutableMap.builder()
                .put(ACCOUNT_NUMBER_KEY, payerFixture.getAccountNumber())
                .put(SORTCODE_KEY, payerFixture.getSortCode())
                .put(NAME_KEY, payerFixture.getName())
                .put(EMAIL_KEY, payerFixture.getEmail())
                .build());

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());

        Map<String, Object> createdPayer = testContext.getDatabaseTestHelper().getPayerByPaymentRequestExternalId(testPaymentRequest.getExternalId());
        String createdPayerExternalId = (String) createdPayer.get("external_id");
        String documentLocation = expectedPayerRequestLocationFor(testPaymentRequest.getExternalId(), createdPayerExternalId);

        response
                .header("Location", is(documentLocation))
                .body("payer_external_id", is(createdPayerExternalId))
                .contentType(JSON);
    }

    @Test
    public void shouldCreateAPayer_forGoCardless() throws JsonProcessingException {
        insertTransactionFixtureWith(GOCARDLESS);
        String fakeCustomerId = "CU000358S3A2FP";
        String fakeBankAccountId = "BA0002WR3Z193A";
        stubCreateCustomer(testPaymentRequest.getExternalId(), payerFixture, fakeCustomerId);
        stubCreateCustomerBankAccount(testPaymentRequest.getExternalId(), payerFixture, fakeCustomerId, fakeBankAccountId);
        String requestPath = "/v1/api/accounts/{accountId}/payment-requests/{paymentRequestExternalId}/payers"
                .replace("{accountId}", testGatewayAccount.getId().toString())
                .replace("{paymentRequestExternalId}", testPaymentRequest.getExternalId());
        String postBody = OBJECT_MAPPER.writeValueAsString(ImmutableMap.builder()
                .put(ACCOUNT_NUMBER_KEY, payerFixture.getAccountNumber())
                .put(SORTCODE_KEY, payerFixture.getSortCode())
                .put(NAME_KEY, payerFixture.getName())
                .put(EMAIL_KEY, payerFixture.getEmail())
                .put(ADDRESS_LINE1_KEY, payerFixture.getAddressLine1())
                .put(ADDRESS_CITY_KEY, payerFixture.getAddressCity())
                .put(ADDRESS_COUNTRY_KEY, payerFixture.getAddressCountry())
                .put(ADDRESS_POSTCODE_KEY, payerFixture.getAddressPostcode())
                .build());

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());

        Map<String, Object> createdPayer = testContext.getDatabaseTestHelper().getPayerByPaymentRequestExternalId(testPaymentRequest.getExternalId());
        String createdPayerExternalId = (String) createdPayer.get("external_id");
        String documentLocation = expectedPayerRequestLocationFor(testPaymentRequest.getExternalId(), createdPayerExternalId);

        response
                .header("Location", is(documentLocation))
                .body("payer_external_id", is(createdPayerExternalId))
                .contentType(JSON);
    }

    private String expectedPayerRequestLocationFor(String paymentRequestExternalId, String payerExternalId) {
        return "http://localhost:" + testContext.getPort() + "/v1/api/accounts/{accountId}/payment-requests/{paymentRequestExternalId}/payers/{payerExternalId}"
                .replace("{accountId}", testGatewayAccount.getId().toString())
                .replace("{paymentRequestExternalId}", paymentRequestExternalId)
                .replace("{payerExternalId}", payerExternalId);
    }

    @Test
    public void shouldReturn400IfMandatoryFieldsMissing() throws JsonProcessingException {
        String postBody = OBJECT_MAPPER.writeValueAsString(ImmutableMap.builder()
                .put(SORTCODE_KEY, payerFixture.getSortCode())
                .put(NAME_KEY, payerFixture.getName())
                .put(EMAIL_KEY, payerFixture.getEmail())
                .build());

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Field(s) missing: [account_number]"));
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
