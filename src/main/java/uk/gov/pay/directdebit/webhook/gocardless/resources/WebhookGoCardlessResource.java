package uk.gov.pay.directdebit.webhook.gocardless.resources;

import com.google.inject.Inject;
import uk.gov.pay.directdebit.webhook.gocardless.support.WebhookVerifier;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;

@Path("/v1/webhooks/gocardless")
public class WebhookGoCardlessResource {

    private WebhookVerifier webhookVerifier;

    @Inject
    public WebhookGoCardlessResource(WebhookVerifier webhookVerifier) {
        this.webhookVerifier = webhookVerifier;
    }

    @POST
    public Response handleWebhook(@HeaderParam("Webhook-Signature") String webhookSignature,
                                  String body) {
        webhookVerifier.verify(body, webhookSignature);
        return Response.status(OK).build();
    }

}
