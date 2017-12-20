package uk.gov.pay.directdebit.mandate.resources;

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
import static uk.gov.pay.directdebit.payments.model.PaymentState.AWAITING_CONFIRMATION;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class ConfirmPaymentResourceIT {

    @DropwizardTestContext
    private TestContext testContext;

    @Test
    public void confirm_shouldCreateAMandateAndUpdateCharge() throws Exception {

        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture().insert(testContext.getJdbi());
        String paymentRequestExternalId = paymentRequestFixture.getExternalId();
        PayerFixture.aPayerFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi());
        Long transactionId = aTransactionFixture().withPaymentRequestId(paymentRequestFixture.getId())
                .withState(AWAITING_CONFIRMATION).insert(testContext.getJdbi()).getId();

        String requestPath = "/v1/api/accounts/20/payment-requests/" + paymentRequestExternalId + "/confirm";

        given().port(testContext.getPort())
                .accept(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionId);
        assertThat(transaction.get("state"), is("SUCCESS"));
    }

    @Test
    public void confirm_shouldFailWhenPayerDoesNotExist() throws Exception {

        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture().insert(testContext.getJdbi());
        aTransactionFixture().withPaymentRequestId(paymentRequestFixture.getId())
                .withState(AWAITING_CONFIRMATION).insert(testContext.getJdbi());

        String requestPath = "/v1/api/accounts/20/payment-requests/" + paymentRequestFixture.getExternalId() + "/confirm";

        given().port(testContext.getPort())
                .accept(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }
}
