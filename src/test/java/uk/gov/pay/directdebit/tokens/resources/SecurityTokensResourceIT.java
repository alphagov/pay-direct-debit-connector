package uk.gov.pay.directdebit.tokens.resources;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.directdebit.infra.IntegrationTest;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.tokens.fixtures.TokenFixture;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.is;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.*;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.tokens.fixtures.TokenFixture.aTokenFixture;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;

public class SecurityTokensResourceIT extends IntegrationTest {
    private TokenFixture testToken;
    private TransactionFixture testTransaction;
    private PaymentRequestFixture testPaymentRequest;

    @Before
    public void setUp() {
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

    private String tokensUrlFor(String id) {
        return SecurityTokensResource.TOKEN_PATH.replace("{chargeTokenId}", id);
    }

}
