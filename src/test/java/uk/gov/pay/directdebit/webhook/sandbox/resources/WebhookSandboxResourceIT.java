package uk.gov.pay.directdebit.webhook.sandbox.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;

import javax.ws.rs.core.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PENDING_DIRECT_DEBIT_PAYMENT;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class WebhookSandboxResourceIT {

    @DropwizardTestContext
    private TestContext testContext;

    @Test
    public void handleWebhook_shouldChangeTheStateToSuccessAndReturn200() throws Exception {
        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture().insert(testContext.getJdbi());
        PayerFixture.aPayerFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi());
        Long transactionId = aTransactionFixture().withPaymentRequestId(paymentRequestFixture.getId())
                .withState(PENDING_DIRECT_DEBIT_PAYMENT).insert(testContext.getJdbi()).getId();

        String requestPath = "/v1/webhooks/sandbox";

        given().port(testContext.getPort())
                .accept(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionId);
        assertThat(transaction.get("state"), is("SUCCESS"));
    }

}
