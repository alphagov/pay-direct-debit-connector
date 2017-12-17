package uk.gov.pay.directdebit.mandate.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;

import javax.ws.rs.core.Response;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;


@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class ConfirmPaymentResourceIT {

    @DropwizardTestContext
    private TestContext testContext;
    private String paymentRequestExternalId;
    private Long transactionId;

    @Before
    public void setup() {
        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture().insert(testContext.getJdbi());
        paymentRequestExternalId = paymentRequestFixture.getExternalId();
        transactionId = aTransactionFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi()).getId();
    }

    @Test
    public void shouldCreateAPaymentRequest() throws Exception {

        String requestPath = "/v1/api/accounts/20/payment-requests/" + paymentRequestExternalId + "/confirm";

        given().port(testContext.getPort())
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionId);
        assertThat(transaction.get("state"), is("CONFIRMED_DIRECT_DEBIT_DETAILS"));
    }
}
