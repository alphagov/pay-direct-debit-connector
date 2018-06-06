package uk.gov.pay.directdebit.mandate.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
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
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.util.ResponseContainsLinkMatcher.containsLink;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class MandateResourceIT {

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
        String returnUrl = "http://service.url/success-page/";
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
                .body("agreement_id", is(notNullValue()))
                .body("agreement_type", is(agreementType))
                .body("return_url", is(returnUrl))
                .body("created_date", is(notNullValue()))
                .contentType(JSON);
        String externalMandateId = response.extract().path("agreement_id").toString();

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
    
//    @Test
//    public void shouldCreateAMandateAndATransaction_IfMandateIsOneOff() throws Exception {
//        String accountExternalId = testGatewayAccount.getExternalId();
//        String expectedReference = "Test reference";
//        String expectedDescription = "Test description";
//        String returnUrl = "http://service.url/success-page/";
//        String postBody = new ObjectMapper().writeValueAsString(ImmutableMap.builder()
//                .put(JSON_AMOUNT_KEY, AMOUNT)
//                .put(JSON_REFERENCE_KEY, expectedReference)
//                .put(JSON_DESCRIPTION_KEY, expectedDescription)
//                .put(JSON_GATEWAY_ACC_KEY, accountExternalId)
//                .put(JSON_RETURN_URL_KEY, returnUrl)
//                .build());
//
//        String requestPath = CHARGES_API_PATH
//                .replace("{accountId}", accountExternalId);
//
//        ValidatableResponse response = givenSetup()
//                .body(postBody)
//                .post(requestPath)
//                .then()
//                .statusCode(Response.Status.CREATED.getStatusCode())
//                .body(JSON_CHARGE_KEY, is(notNullValue()))
//                .body(JSON_AMOUNT_KEY, isNumber(AMOUNT))
//                .body(JSON_REFERENCE_KEY, is(expectedReference))
//                .body(JSON_DESCRIPTION_KEY, is(expectedDescription))
//                .body(JSON_RETURN_URL_KEY, is(returnUrl))
//                .contentType(JSON);
//
//        String externalTransactionId = response.extract().path(JSON_CHARGE_KEY).toString();
//
//        Map<String, Object> createdTransaction = testContext.getDatabaseTestHelper().getTransactionByExternalId(externalTransactionId);
//        assertThat(createdTransaction.get("external_id"), is(notNullValue()));
//        assertThat(createdTransaction.get("reference"), is(expectedReference));
//        assertThat(createdTransaction.get("description"), is(expectedDescription));
//        assertThat(createdTransaction.get("amount"), is(AMOUNT));
//
//        Map<String, Object> createdMandate = testContext.getDatabaseTestHelper().getMandateByTransactionExternalId(externalTransactionId);
//
//        assertThat(createdMandate.get("external_id"), is(notNullValue()));
//        assertThat(createdMandate.get("reference"), is(expectedReference));
//        assertThat(createdMandate.get("description"), is(expectedDescription));
//        assertThat(createdMandate.get("return_url"), is(returnUrl));
//        assertThat(createdMandate.get("gateway_account_id"), is(testGatewayAccount.getId()));
//        assertThat(createdMandate.get("payer"), is(nullValue()));
//    }
    
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
