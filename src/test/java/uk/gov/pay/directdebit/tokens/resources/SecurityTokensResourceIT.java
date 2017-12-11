package uk.gov.pay.directdebit.tokens.resources;

import io.dropwizard.jdbi.OptionalContainerFactory;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardPortValue;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.tokens.fixtures.TokenFixture;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.is;
import static uk.gov.pay.directdebit.junit.DropwizardJUnitRunner.getDbConfig;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.tokens.fixtures.TokenFixture.aTokenFixture;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class SecurityTokensResourceIT {

    private TokenFixture testToken;
    private TransactionFixture testTransaction;
    private PaymentRequestFixture testPaymentRequest;
    private DBI jdbi;

    @DropwizardPortValue
    private int port;

    @Before
    public void setUp() {
        jdbi = new DBI(getDbConfig().getUrl(), getDbConfig().getUser(), getDbConfig().getPassword());
        jdbi.registerContainerFactory(new OptionalContainerFactory());
        testPaymentRequest = aPaymentRequestFixture().insert(jdbi);
        testTransaction = aTransactionFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .withExternalId(testPaymentRequest.getExternalId()).insert(jdbi);
        testToken = aTokenFixture().withPaymentRequestId(testPaymentRequest.getId()).insert(jdbi);
    }

    @Test
    public void shouldReturn200WithPaymentRequestForValidToken() {
        givenSetup()
                .get(tokensUrlFor(testToken.getToken()) + "/charge")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("external_id", is(testTransaction.getPaymentRequestExternalId()))
                .body("state", is(PaymentState.ENTERING_DIRECT_DEBIT_DETAILS.toString()))
                .body("type", is(testTransaction.getType().toString()))
                .body("amount", isNumber(testTransaction.getAmount()));
    }

    @Test
    public void shouldReturnNoContentWhenDeletingToken() {
        givenSetup()
                .delete(tokensUrlFor(testToken.getToken()))
                .then()
                .statusCode(204);
    }

    private RequestSpecification givenSetup() {
        return given().port(port)
                .contentType(JSON);
    }

    private String tokensUrlFor(String id) {
        return SecurityTokensResource.TOKEN_PATH.replace("{chargeTokenId}", id);
    }

}
