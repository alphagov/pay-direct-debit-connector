package uk.gov.pay.directdebit.payers.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PayerResourceIT {
    private final static String ACCOUNT_NUMBER_KEY = "account_number";
    private final static String SORT_CODE_KEY = "sort_code";
    private final static String NAME_KEY = "account_holder_name";
    private final static String EMAIL_KEY = "email";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @DropwizardTestContext
    private TestContext testContext;

    //todo we should be able to override this in the test-it-config or else tests won't easily run in parallel. See https://payments-platform.atlassian.net/browse/PP-3374
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(10107);

    private GatewayAccountFixture testGatewayAccount;
    private MandateFixture testMandate;
    private PayerFixture payerFixture = aPayerFixture().withAccountNumber("12345678");

    private void createPayerFor(PaymentProvider paymentProvider) throws JsonProcessingException {
        createGatewayAccountWithTransactionAndRequestPath(paymentProvider);
        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/payers"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace("{mandateExternalId}", testMandate.getExternalId().toString());
        String putBody = OBJECT_MAPPER.writeValueAsString(ImmutableMap.builder()
                .put(ACCOUNT_NUMBER_KEY, payerFixture.getAccountNumber())
                .put(SORT_CODE_KEY, payerFixture.getSortCode())
                .put(NAME_KEY, payerFixture.getName())
                .put(EMAIL_KEY, payerFixture.getEmail())
                .build());

        ValidatableResponse response = givenSetup()
                .body(putBody)
                .put(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());

        Map<String, Object> createdPayer = testContext.getDatabaseTestHelper().getPayerByMandateExternalId(testMandate.getExternalId());
        String createdPayerExternalId = (String) createdPayer.get("external_id");
        String documentLocation = expectedPayerRequestLocationFor(testMandate.getExternalId(), createdPayerExternalId);

        response
                .header("Location", is(documentLocation))
                .body("payer_external_id", is(createdPayerExternalId))
                .contentType(JSON);
    }

    @Test
    public void shouldCreateAPayer() throws JsonProcessingException {
        createPayerFor(SANDBOX);
    }

    @Test
    public void shouldCreateAPayer_forGoCardless() throws JsonProcessingException {
        createPayerFor(GOCARDLESS);
    }

    private String expectedPayerRequestLocationFor(MandateExternalId mandateExternalId, String payerExternalId) {
        return "http://localhost:" + testContext.getPort() + "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/payers/{payerExternalId}"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace("{mandateExternalId}", mandateExternalId.toString())
                .replace("{payerExternalId}", payerExternalId);
    }

    @Test
    public void shouldReturn400IfMandatoryFieldsMissingWhenCreatingPayer() throws JsonProcessingException {
        createGatewayAccountWithTransactionAndRequestPath(SANDBOX);
        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/payers"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace("{mandateExternalId}", testMandate.getExternalId().toString());
        String putBody = OBJECT_MAPPER.writeValueAsString(ImmutableMap.builder()
                .put(SORT_CODE_KEY, payerFixture.getSortCode())
                .put(NAME_KEY, payerFixture.getName())
                .put(EMAIL_KEY, payerFixture.getEmail())
                .build());

        givenSetup()
                .body(putBody)
                .put(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", contains("Field(s) missing: [account_number]"))
                .body("error_identifier", is(ErrorIdentifier.GENERIC.toString()));
    }

    @Test
    public void shouldReturn400IfMandatoryFieldsMissingWhenValidatingBankAccount() throws JsonProcessingException {
        createGatewayAccountWithTransactionAndRequestPath(SANDBOX);
        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/payers/bank-account/validate"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace("{mandateExternalId}", testMandate.getExternalId().toString());
        String putBody = OBJECT_MAPPER.writeValueAsString(ImmutableMap.builder()
                .put(ACCOUNT_NUMBER_KEY, payerFixture.getAccountNumber())
                .build());

        givenSetup()
                .body(putBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", contains("Field(s) missing: [sort_code]"))
                .body("error_identifier", is(ErrorIdentifier.GENERIC.toString()));
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }

    private void createGatewayAccountWithTransactionAndRequestPath(PaymentProvider paymentProvider) {
        testGatewayAccount = aGatewayAccountFixture()
                .withPaymentProvider(paymentProvider)
                .insert(testContext.getJdbi());
        testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount).insert(testContext.getJdbi());
        aPaymentFixture()
                .withMandateFixture(testMandate)
                .withState(PaymentState.CREATED)
                .insert(testContext.getJdbi());
    }
}
