package uk.gov.pay.directdebit.webhook.sandbox.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;

import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class WebhookSandboxResourceIT {

    @DropwizardTestContext
    private TestContext testContext;

    @Test
    public void handleWebhook_shouldReturn200() throws Exception {
        String requestPath = "/v1/webhooks/sandbox";

        given().port(testContext.getPort())
                .accept(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

}
