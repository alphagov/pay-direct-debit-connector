package uk.gov.pay.directdebit.payers.resources;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

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
    private final String accountId = "20";
    private Gson gson = new Gson();

    @DropwizardTestContext
    private TestContext testContext;

    private PaymentRequestFixture testPaymentRequest;
    private PayerFixture payerFixture;

    String requestPath;
    @Before
    public void setUp() {
        testPaymentRequest = aPaymentRequestFixture().insert(testContext.getJdbi());
        aTransactionFixture()
                .withState(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withPaymentRequestId(testPaymentRequest.getId())
                .withPaymentRequestExternalId(testPaymentRequest.getExternalId()).insert(testContext.getJdbi());
        payerFixture = aPayerFixture().withAccountNumber("12345678");
        requestPath = "/v1/api/accounts/{accountId}/payment-requests/{paymentRequestExternalId}/payers"
                .replace("{accountId}", accountId)
                .replace("{paymentRequestExternalId}", testPaymentRequest.getExternalId());

    }
    @Test
    public void shouldCreateAPayer() throws Exception {
        String postBody = gson.toJson(ImmutableMap.builder()
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
                .replace("{accountId}", accountId)
                .replace("{paymentRequestExternalId}", paymentRequestExternalId)
                .replace("{payerExternalId}", payerExternalId);
    }
    @Test
    public void shouldReturn400IfMandatoryFieldsMissing() {
        String postBody = gson.toJson(ImmutableMap.builder()
                .put(SORTCODE_KEY, payerFixture.getSortCode())
                .put(NAME_KEY, payerFixture.getName())
                .put(EMAIL_KEY, payerFixture.getEmail())
                .put(ADDRESS_LINE1_KEY, payerFixture.getAddressLine1())
                .put(ADDRESS_LINE2_KEY, payerFixture.getAddressLine2())
                .put(ADDRESS_CITY_KEY, payerFixture.getAddressCity())
                .put(ADDRESS_POSTCODE_KEY, payerFixture.getAddressPostcode())
                .build());

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Field(s) missing: [account_number, country_code]"));
    }

    @Test
    public void shouldReturn400IfFieldsInvalidSize() {
        String postBody = gson.toJson(ImmutableMap.builder()
                .put(ACCOUNT_NUMBER_KEY, payerFixture.getAccountNumber())
                .put(SORTCODE_KEY, payerFixture.getSortCode())
                .put(NAME_KEY, payerFixture.getName())
                .put(EMAIL_KEY, RandomStringUtils.randomAlphabetic(255))
                .put(ADDRESS_LINE1_KEY, payerFixture.getAddressLine1())
                .put(ADDRESS_CITY_KEY, payerFixture.getAddressCity())
                .put(ADDRESS_COUNTRY_KEY, payerFixture.getAddressCountry())
                .put(ADDRESS_POSTCODE_KEY, payerFixture.getAddressPostcode())
                .build());

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Field(s) are too big: [email]"));
    }

    @Test
    public void shouldReturn400IfFieldsInvalid() {
        String postBody = gson.toJson(ImmutableMap.builder()
                .put(ACCOUNT_NUMBER_KEY, "123c5678")
                .put(SORTCODE_KEY, "123a56")
                .put(NAME_KEY, payerFixture.getName())
                .put(EMAIL_KEY, payerFixture.getEmail())
                .put(ADDRESS_LINE1_KEY, payerFixture.getAddressLine1())
                .put(ADDRESS_CITY_KEY, payerFixture.getAddressCity())
                .put(ADDRESS_COUNTRY_KEY, payerFixture.getAddressCountry())
                .put(ADDRESS_POSTCODE_KEY, payerFixture.getAddressPostcode())
                .build());

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Field(s) are invalid: [account_number, sort_code]"));
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
