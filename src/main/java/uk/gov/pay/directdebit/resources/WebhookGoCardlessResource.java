package uk.gov.pay.directdebit.resources;

import com.google.inject.Inject;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.core.V1ApiPaths;
import uk.gov.pay.directdebit.app.core.WebhookVerifier;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;

@Path(V1ApiPaths.WEBHOOKS_GOCARDLESS_PATH)
public class WebhookGoCardlessResource {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(WebhookGoCardlessResource.class);

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
