package uk.gov.pay.directdebit.webhook.sandbox.resources;

import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class WebhookSandboxResourceIT {

    @DropwizardTestContext
    private TestContext testContext;

    @Test
    public void handleWebhook_shouldChangeTheStateToSuccessAndReturn200() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());
        MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
        PayerFixture.aPayerFixture().withMandateId(mandateFixture.getId()).insert(testContext.getJdbi());
        Long transactionId = aPaymentFixture().withMandateFixture(mandateFixture)
                .withState(PaymentState.PENDING).insert(testContext.getJdbi()).getId();

        String requestPath = "/v1/webhooks/sandbox";

        given().port(testContext.getPort())
                .accept(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        Map<String, Object> payment = testContext.getDatabaseTestHelper().getPaymentById(transactionId);
        assertThat(payment.get("state"), is("SUCCESS"));
    }

}
