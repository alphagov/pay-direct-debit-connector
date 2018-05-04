package uk.gov.pay.directdebit.payments.resources;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.payments.resources.PaymentRequestResource.CANCEL_CHARGE_API_PATH;
import static uk.gov.pay.directdebit.payments.resources.PaymentRequestResource.CHARGES_API_PATH;
import static uk.gov.pay.directdebit.payments.resources.PaymentRequestResource.CHARGE_API_PATH;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;
import static uk.gov.pay.directdebit.util.ResponseContainsLinkMatcher.containsLink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
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
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;


@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentRequestResourceIT {

    private static final String FRONTEND_CARD_DETAILS_URL = "/secure";
    private static final String JSON_AMOUNT_KEY = "amount";
    private static final String JSON_REFERENCE_KEY = "reference";
    private static final String JSON_DESCRIPTION_KEY = "description";
    private static final String JSON_GATEWAY_ACC_KEY = "gateway_account_id";
    private static final String JSON_RETURN_URL_KEY = "return_url";
    private static final String JSON_CHARGE_KEY = "charge_id";
    private static final String JSON_STATE_KEY = "state.status";
    private static final long AMOUNT = 6234L;
    private GatewayAccountFixture testGatewayAccount;

    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setUp() {
        testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
    }
   
    @Test
    public void shouldCreateAPaymentRequest() throws Exception {

        String accountExternalId = testGatewayAccount.getExternalId();
        String expectedReference = "Test reference";
        String expectedDescription = "Test description";
        String returnUrl = "http://service.url/success-page/";
        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, AMOUNT)
                .put(JSON_REFERENCE_KEY, expectedReference)
                .put(JSON_DESCRIPTION_KEY, expectedDescription)
                .put(JSON_GATEWAY_ACC_KEY, accountExternalId)
                .put(JSON_RETURN_URL_KEY, returnUrl)
                .build());

        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountExternalId);

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body(JSON_CHARGE_KEY, is(notNullValue()))
                .body(JSON_AMOUNT_KEY, isNumber(AMOUNT))
                .body(JSON_REFERENCE_KEY, is(expectedReference))
                .body(JSON_DESCRIPTION_KEY, is(expectedDescription))
                .body(JSON_RETURN_URL_KEY, is(returnUrl))
                .contentType(JSON);

        String externalPaymentRequestId = response.extract().path(JSON_CHARGE_KEY).toString();
        
        Map<String, Object> createdPaymentRequest = testContext.getDatabaseTestHelper().getPaymentRequestByExternalId(externalPaymentRequestId);
        
        assertThat(createdPaymentRequest.get("external_id"), is(externalPaymentRequestId));
        assertThat(createdPaymentRequest.get("reference"), is(expectedReference));
        assertThat(createdPaymentRequest.get("description"), is(expectedDescription));
        assertThat(createdPaymentRequest.get("amount"), is(AMOUNT));
        assertThat(createdPaymentRequest.get("return_url"), is(returnUrl));
        assertThat(createdPaymentRequest.get("gateway_account_id"), is(testGatewayAccount.getId()));
        assertThat(createdPaymentRequest.get("payer"), is(nullValue()));
    }

    @Test
    public void shouldRetrieveAPaymentRequest_fromPublicApiEndpoint() {

        String accountExternalId = testGatewayAccount.getExternalId();
        
        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture()
                .withGatewayAccountId(testGatewayAccount.getId())
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture()
                .withPaymentRequestId(paymentRequestFixture.getId())
                .insert(testContext.getJdbi());

        String requestPath = CHARGE_API_PATH
                .replace("{accountId}", accountExternalId)
                .replace("{paymentRequestExternalId}", paymentRequestFixture.getExternalId());

        ValidatableResponse getChargeResponse = givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body(JSON_CHARGE_KEY, is(paymentRequestFixture.getExternalId()))
                .body(JSON_AMOUNT_KEY, isNumber(paymentRequestFixture.getAmount()))
                .body(JSON_REFERENCE_KEY, is(paymentRequestFixture.getReference()))
                .body(JSON_DESCRIPTION_KEY, is(paymentRequestFixture.getDescription()))
                .body(JSON_STATE_KEY, is(transactionFixture.getState().toExternal().getState()))
                .body(JSON_RETURN_URL_KEY, is(paymentRequestFixture.getReturnUrl()));


        String documentLocation = expectedPaymentRequestLocationFor(accountExternalId, paymentRequestFixture.getExternalId());
        String token = testContext.getDatabaseTestHelper().getTokenByPaymentRequestExternalId(paymentRequestFixture.getExternalId());

        String hrefNextUrl = "http://Frontend" + FRONTEND_CARD_DETAILS_URL + "/" + token;
        String hrefNextUrlPost = "http://Frontend" + FRONTEND_CARD_DETAILS_URL;


        getChargeResponse
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", hrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", token);
                }}));
        
        String requestPath2 = CHARGE_API_PATH
                .replace("{accountId}", accountExternalId)
                .replace("{paymentRequestExternalId}", paymentRequestFixture.getExternalId());

        ValidatableResponse getChargeFromTokenResponse = givenSetup()
                .get(requestPath2)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body(JSON_CHARGE_KEY, is(paymentRequestFixture.getExternalId()))
                .body(JSON_AMOUNT_KEY, isNumber(paymentRequestFixture.getAmount()))
                .body(JSON_REFERENCE_KEY, is(paymentRequestFixture.getReference()))
                .body(JSON_DESCRIPTION_KEY, is(paymentRequestFixture.getDescription()))
                .body(JSON_STATE_KEY, is(transactionFixture.getState().toExternal().getState()))
                .body(JSON_RETURN_URL_KEY, is(paymentRequestFixture.getReturnUrl()));

        String newChargeToken = testContext.getDatabaseTestHelper().getTokenByPaymentRequestExternalId(paymentRequestFixture.getExternalId());

        String newHrefNextUrl = "http://Frontend" + FRONTEND_CARD_DETAILS_URL + "/" + newChargeToken;

        getChargeFromTokenResponse
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", newHrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", newChargeToken);
                }}));
    }

    @Test
    public void shouldCancelATransaction() {
        PaymentRequestFixture paymentRequestFixture = getPaymentRequestFixture();
        TransactionFixture transactionFixture = getTransactionFixture(paymentRequestFixture.getId(), PaymentState.AWAITING_DIRECT_DEBIT_DETAILS);

        String requestPath = CANCEL_CHARGE_API_PATH
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace("{paymentRequestExternalId}", paymentRequestFixture.getExternalId());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        assertThat(transaction.get("state"), is("CANCELLED"));
    }
    
    @Test
    public void shouldRetrieveAPaymentRequest_FromFrontendEndpoint() {

        String accountExternalId = testGatewayAccount.getExternalId();

        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture()
                .withGatewayAccountId(testGatewayAccount.getId())
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture()
                .withPaymentRequestId(paymentRequestFixture.getId())
                .insert(testContext.getJdbi());

        String frontendPaymentRequestPath = "/v1/accounts/{accountId}/payment-requests/{paymentRequestExternalId}";
        String requestPath = frontendPaymentRequestPath
                .replace("{accountId}", accountExternalId)
                .replace("{paymentRequestExternalId}", paymentRequestFixture.getExternalId());

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body("external_id", is(paymentRequestFixture.getExternalId()))
                .body("gateway_account_id", isNumber(testGatewayAccount.getId()))
                .body("gateway_account_external_id", is(testGatewayAccount.getExternalId()))
                .body(JSON_AMOUNT_KEY, isNumber(paymentRequestFixture.getAmount()))
                .body(JSON_REFERENCE_KEY, is(paymentRequestFixture.getReference()))
                .body(JSON_DESCRIPTION_KEY, is(paymentRequestFixture.getDescription()))
                .body(JSON_STATE_KEY, is(transactionFixture.getState().toExternal().getState()))
                .body(JSON_RETURN_URL_KEY, is(paymentRequestFixture.getReturnUrl()))
                .body("payer.payer_external_id", is(payerFixture.getExternalId()))
                .body("payer.account_holder_name", is(payerFixture.getName()))
                .body("payer.email", is(payerFixture.getEmail()))
                .body("payer.requires_authorisation", is(payerFixture.getAccountRequiresAuthorisation()));
    }
    
    @Test
    public void shouldReturn400IfMandatoryFieldsMissing() throws JsonProcessingException {
        String accountId = testGatewayAccount.getId().toString();

        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, AMOUNT)
                .put(JSON_DESCRIPTION_KEY, "desc")
                .put(JSON_GATEWAY_ACC_KEY, accountId)
                .put(JSON_RETURN_URL_KEY, "http://service.url/success-page/")
                .build());
        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountId);

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Field(s) missing: [reference]"));
    }

    @Test
    public void shouldReturn400IfFieldsInvalidSize() throws JsonProcessingException {
        String accountId = testGatewayAccount.getId().toString();

        String postBody =  new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, AMOUNT)
                .put(JSON_REFERENCE_KEY, "reference")
                .put(JSON_DESCRIPTION_KEY, RandomStringUtils.randomAlphabetic(256))
                .put(JSON_GATEWAY_ACC_KEY, accountId)
                .put(JSON_RETURN_URL_KEY, "http://service.url/success-page/")
                .build());
        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountId);

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("The size of a field(s) is invalid: [description]"));
    }

    @Test
    public void shouldReturn400IfFieldsInvalid() throws JsonProcessingException {
        String accountId = testGatewayAccount.getId().toString();

        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, 10000001)
                .put(JSON_REFERENCE_KEY, "reference")
                .put(JSON_DESCRIPTION_KEY, "desc")
                .put(JSON_GATEWAY_ACC_KEY, accountId)
                .put(JSON_RETURN_URL_KEY, "http://service.url/success-page/")
                .build());
        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountId);

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Field(s) are invalid: [amount]"));
    }

    @Test
    public void shouldChangePaymentType() {
        PaymentRequestFixture paymentRequestFixture = getPaymentRequestFixture();
        TransactionFixture transactionFixture = getTransactionFixture(paymentRequestFixture.getId(), PaymentState.AWAITING_DIRECT_DEBIT_DETAILS);

        String requestPath = "/v1/api/accounts/{accountId}/payment-requests/{paymentRequestExternalId}/change-payment-method"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace("{paymentRequestExternalId}", paymentRequestFixture.getExternalId());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        assertThat(transaction.get("state"), is("USER_CANCEL_NOT_ELIGIBLE"));
    }

    private PaymentRequestFixture getPaymentRequestFixture() {
        return aPaymentRequestFixture()
                    .withGatewayAccountId(testGatewayAccount.getId())
                    .insert(testContext.getJdbi());
    }

    private TransactionFixture getTransactionFixture(long paymentRequestId, PaymentState paymentState) {
        return aTransactionFixture()
                .withGatewayAccountId(testGatewayAccount.getId())
                .withState(paymentState)
                .withPaymentRequestId(paymentRequestId)
                .insert(testContext.getJdbi());
    }

    private String expectedPaymentRequestLocationFor(String accountId, String chargeId) {
        return "http://localhost:" + testContext.getPort() + CHARGE_API_PATH
                .replace("{accountId}", accountId)
                .replace("{paymentRequestExternalId}", chargeId);
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
