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
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.tokens.fixtures.TokenFixture;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.tokens.fixtures.TokenFixture.aTokenFixture;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class SecurityTokensResourceIT {

    private TokenFixture testToken;
    private GatewayAccountFixture testGatewayAccount;
    
    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setUp() {
        testGatewayAccount = aGatewayAccountFixture().insert(testContext.getJdbi());
    }

    @Test
    public void shouldReturn200WithMandateForValidToken_ifMandateIsOnDemand() {
        MandateFixture testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());
        TokenFixture testToken = aTokenFixture().withMandateId(testMandate.getId()).insert(testContext.getJdbi());
        String requestPath = "/v1/tokens/{token}/mandate".replace("{token}", testToken.getToken());
        givenSetup()
                .get(requestPath)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("external_id", is(testMandate.getExternalId().toString()))
                .body("mandate_reference", is(testMandate.getMandateReference().toString()))
                .body("return_url", is(testMandate.getReturnUrl()))
                .body("gateway_account_id", isNumber(testGatewayAccount.getId()))
                .body("gateway_account_external_id", is(testGatewayAccount.getExternalId()))
                .body("state", is(MandateState.AWAITING_DIRECT_DEBIT_DETAILS.toString()))
                .body("$", not(hasKey("transaction_external_id")));
    }
    
    @Test
    public void shouldReturnNoContentWhenDeletingToken() {
        MandateFixture testMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(testGatewayAccount)
                .insert(testContext.getJdbi());
        TokenFixture testToken = aTokenFixture().withMandateId(testMandate.getId()).insert(testContext.getJdbi());
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
