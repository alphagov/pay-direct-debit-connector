package uk.gov.pay.directdebit.webhook.sandbox.resources;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;

@Path("/v1/webhooks/sandbox")
public class WebhookSandboxResource {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(WebhookSandboxResource.class);

    @POST
    public Response handleWebhook() {
        return Response.status(OK).build();
    }

}
