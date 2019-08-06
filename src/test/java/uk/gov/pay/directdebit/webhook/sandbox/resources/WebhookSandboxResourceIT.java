package uk.gov.pay.directdebit.webhook.sandbox.resources;

import org.junit.Test;
import org.junit.After;
import org.junit.Rule;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.junit.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import javax.ws.rs.core.Response;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

public class WebhookSandboxResourceIT {

    @Rule
    public DropwizardAppWithPostgresRule app = new DropwizardAppWithPostgresRule();

    @After
    public void tearDown() {
        DatabaseTestHelper databaseHelper = app.getDatabaseTestHelper();
        databaseHelper.truncateAllData();
    }

    @Test
    public void handleWebhook_shouldChangeTheStateToSuccessAndReturn200() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
        .withPaymentProvider(SANDBOX)
        .withOrganisation(null)
        .insert(app.getTestContext().getJdbi());
        MandateFixture mandateFixture = MandateFixture.aMandateFixture()
        .withGatewayAccountFixture(gatewayAccountFixture)
        .insert(app.getTestContext().getJdbi());
        PayerFixture.aPayerFixture()
        .withMandateId(mandateFixture.getId())
        .insert(app.getTestContext().getJdbi());

        String sandboxPaymentExternalAndProviderId = RandomIdGenerator.newId();
        Long paymentId = aPaymentFixture()
                .withExternalId(sandboxPaymentExternalAndProviderId)
                .withPaymentProviderId(SandboxPaymentId.valueOf(sandboxPaymentExternalAndProviderId))
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.SUBMITTED_TO_PROVIDER)
                .insert(app.getTestContext().getJdbi()).getId();

        String requestPath = "/v1/webhooks/sandbox";

        given().port(app.getTestContext().getPort())
                .accept(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        Map<String, Object> payment = app.getTestContext().getDatabaseTestHelper().getPaymentById(paymentId);
        assertThat(payment.get("state"), is("PAID_OUT"));
    }

}
