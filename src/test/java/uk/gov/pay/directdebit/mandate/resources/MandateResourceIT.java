package uk.gov.pay.directdebit.mandate.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;
import static uk.gov.pay.directdebit.util.ResponseContainsLinkMatcher.containsLink;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class MandateResourceIT {

    private static final String JSON_AMOUNT_KEY = "amount";
    private static final String JSON_REFERENCE_KEY = "reference";
    private static final String JSON_DESCRIPTION_KEY = "description";
    private static final String JSON_STATE_KEY = "state.status";
    private static final String JSON_MANDATE_ID_KEY = "mandate_id";
    private GatewayAccountFixture testGatewayAccount;

    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setUp() {
        testGatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
    }

    @Test
    public void shouldCreateAMandateWithoutTransaction_IfMandateIsOnDemand() throws Exception {
        String accountExternalId = testGatewayAccount.getExternalId();
        String agreementType = MandateType.ON_DEMAND.toString();
        String returnUrl = "http://example.com/success-page/";
        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
                .put("agreement_type", agreementType)
                .put("return_url", returnUrl)
                .build());

        String requestPath = "/v1/api/accounts/{accountId}/mandates"
                .replace("{accountId}", accountExternalId);
        
        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body(JSON_MANDATE_ID_KEY, is(notNullValue()))
                .body("mandate_type", is(agreementType))
                .body("return_url", is(returnUrl))
                .body("created_date", is(notNullValue()))
                .contentType(JSON);
        String externalMandateId = response.extract().path(JSON_MANDATE_ID_KEY).toString();

        String documentLocation = expectedMandateLocationFor(accountExternalId, externalMandateId);
        String token = testContext.getDatabaseTestHelper().getTokenByMandateExternalId(externalMandateId).get("secure_redirect_token").toString();

        String hrefNextUrl = "http://Frontend/secure/" + token;
        String hrefNextUrlPost = "http://Frontend/secure";


        response
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", hrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", token);
                }}));
        
        Map<String, Object> createdMandate = testContext.getDatabaseTestHelper().getMandateByExternalId(externalMandateId);

        assertThat(createdMandate.get("external_id"), is(notNullValue()));
        assertThat(createdMandate.get("return_url"), is(returnUrl));
        assertThat(createdMandate.get("gateway_account_id"), is(testGatewayAccount.getId()));
        assertThat(createdMandate.get("payer"), is(nullValue()));
        assertThat(createdMandate.get("transaction"), is(nullValue()));
    }

    
    
    @Test
    public void shouldRetrieveAMandate_FromFrontendEndpoint_WhenATransactionHasBeenCreated() {
        String accountExternalId = testGatewayAccount.getExternalId();
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withPayerFixture(payerFixture)
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = createTransactionFixtureWith(mandateFixture, PaymentState.NEW);

        String frontendMandateWithTransationPath = "/v1/accounts/{accountId}/mandates/{mandateExternalId}/payments/{transactionExternalId}";
        String requestPath = frontendMandateWithTransationPath
                .replace("{accountId}", accountExternalId)
                .replace("{mandateExternalId}", mandateFixture.getExternalId())
                .replace("{transactionExternalId}", transactionFixture.getExternalId());

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body("external_id", is(mandateFixture.getExternalId()))
                .body("gateway_account_id", isNumber(testGatewayAccount.getId()))
                .body("gateway_account_external_id", is(testGatewayAccount.getExternalId()))
                .body("state.status", is(mandateFixture.getState().toExternal().getState()))
                .body("reference", is(mandateFixture.getReference()))
                .body("created_date", is(mandateFixture.getCreatedDate().toString()))
                .body("transaction." + JSON_AMOUNT_KEY, isNumber(transactionFixture.getAmount()))
                .body("transaction." + JSON_REFERENCE_KEY, is(transactionFixture.getReference()))
                .body("transaction." + JSON_DESCRIPTION_KEY, is(transactionFixture.getDescription()))
                .body("transaction." + JSON_STATE_KEY, is(transactionFixture.getState().toExternal().getState()))
                .body("payer.payer_external_id", is(payerFixture.getExternalId()))
                .body("payer.account_holder_name", is(payerFixture.getName()))
                .body("payer.email", is(payerFixture.getEmail()))
                .body("payer.requires_authorisation", is(payerFixture.getAccountRequiresAuthorisation()));
    }

    @Test
    public void shouldRetrieveAMandate_FromFrontendEndpoint_WhenNoTransactionHasBeenCreated() {

        String accountExternalId = testGatewayAccount.getExternalId();
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withPayerFixture(payerFixture)
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        String frontendMandatePath = "/v1/accounts/{accountId}/mandates/{mandateExternalId}";
        String requestPath = frontendMandatePath
                .replace("{accountId}", accountExternalId)
                .replace("{mandateExternalId}", mandateFixture.getExternalId());

        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body("external_id", is(mandateFixture.getExternalId()))
                .body("gateway_account_id", isNumber(testGatewayAccount.getId()))
                .body("gateway_account_external_id", is(testGatewayAccount.getExternalId()))
                .body("state.status", is(mandateFixture.getState().toExternal().getState()))
                .body("reference", is(mandateFixture.getReference()))
                .body("created_date", is(mandateFixture.getCreatedDate().toString()))
                .body("$", not(hasKey("transaction")))
                .body("payer.payer_external_id", is(payerFixture.getExternalId()))
                .body("payer.account_holder_name", is(payerFixture.getName()))
                .body("payer.email", is(payerFixture.getEmail()))
                .body("payer.requires_authorisation", is(payerFixture.getAccountRequiresAuthorisation()));
    }
    
    @Test
    public void shouldCancelAMandate() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ON_DEMAND)
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/cancel"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace("{mandateExternalId}", mandateFixture.getExternalId());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(transaction.get("state"), is("CANCELLED"));
    }

    @Test
    public void shouldCancelAMandateAndTransaction_ifMandateIsOneOff() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ONE_OFF)
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());
        
        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/cancel"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace("{mandateExternalId}", mandateFixture.getExternalId());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());


        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(mandateFixture.getId());
        assertThat(mandate.get("state"), is("CANCELLED"));
        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        assertThat(transaction.get("state"), is("CANCELLED"));
    }

    @Test
    public void shouldChangePaymentType() {
        MandateFixture testMandate = MandateFixture.aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withMandateType(MandateType.ON_DEMAND)
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/change-payment-method"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace("{mandateExternalId}", testMandate.getExternalId());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getMandateById(testMandate.getId());
        assertThat(transaction.get("state"), is("USER_CANCEL_NOT_ELIGIBLE"));
    }

    @Test
    public void shouldChangePaymentType_ifMandateIsOneOff() {
        MandateFixture testMandate = MandateFixture.aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withMandateType(MandateType.ONE_OFF)
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());

        TransactionFixture transactionFixture = aTransactionFixture()
                .withMandateFixture(testMandate)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());
        
        String requestPath = "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/change-payment-method"
                .replace("{accountId}", testGatewayAccount.getExternalId())
                .replace("{mandateExternalId}", testMandate.getExternalId());

        givenSetup()
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(testMandate.getId());
        assertThat(mandate.get("state"), is("USER_CANCEL_NOT_ELIGIBLE"));
        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        assertThat(transaction.get("state"), is("USER_CANCEL_NOT_ELIGIBLE"));
    }
    private TransactionFixture createTransactionFixtureWith(MandateFixture mandateFixture, PaymentState paymentState) {
        return aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(paymentState)
                .insert(testContext.getJdbi());
    }

    private String expectedMandateLocationFor(String accountId, String mandateExternalId) {
        return "http://localhost:" + testContext.getPort() + "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}"
                .replace("{accountId}", accountId)
                .replace("{mandateExternalId}", mandateExternalId);
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }


}
