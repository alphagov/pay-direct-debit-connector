package uk.gov.pay.directdebit.tokens.resources;

import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.tokens.fixtures.TokenFixture;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.is;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.tokens.fixtures.TokenFixture.aTokenFixture;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class SecurityTokensResourceIT {

    private TokenFixture testToken;
    private TransactionFixture testTransaction;
    private GatewayAccountFixture testGatewayAccount;
    private PaymentRequestFixture testPaymentRequest;

    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setUp() {
        testGatewayAccount = aGatewayAccountFixture().insert(testContext.getJdbi());
        testPaymentRequest = aPaymentRequestFixture().withGatewayAccountId(testGatewayAccount.getId()).insert(testContext.getJdbi());
        testTransaction = aTransactionFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .withPaymentRequestExternalId(testPaymentRequest.getExternalId())
                .withPaymentRequestReturnUrl(testPaymentRequest.getReturnUrl())
                .withPaymentRequestGatewayAccountId(testPaymentRequest.getGatewayAccountId())
                .withPaymentRequestDescription(testPaymentRequest.getDescription()  )
                .insert(testContext.getJdbi());
        testToken = aTokenFixture().withPaymentRequestId(testPaymentRequest.getId()).insert(testContext.getJdbi());
    }

    @Test
    public void shouldReturn200WithPaymentRequestForValidToken() {
        String requestPath = "/v1/tokens/{token}/payment-request".replace("{token}", testToken.getToken());
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("external_id", is(testTransaction.getPaymentRequestExternalId()))
                .body("description", is(testTransaction.getPaymentRequestDescription()))
                .body("return_url", is(testTransaction.getPaymentRequestReturnUrl()))
                .body("gateway_account_id", isNumber(testTransaction.getPaymentRequestGatewayAccountId()))
                .body("state", is(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS.toString()))
                .body("type", is(testTransaction.getType().toString()))
                .body("amount", isNumber(testTransaction.getAmount()));
    }

    @Test
    public void shouldReturnNoContentWhenDeletingToken() {
        String requestPath = "/v1/tokens/{token}".replace("{token}", testToken.getToken());
        givenSetup()
                .delete(requestPath)
                .then()
                .statusCode(204);
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
