package uk.gov.pay.directdebit.webhook.gocardless.resources;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.webhook.gocardless.api.GoCardlessWebhookParser;
import uk.gov.pay.directdebit.webhook.gocardless.services.WebhookGoCardlessService;
import uk.gov.pay.directdebit.webhook.gocardless.support.GoCardlessWebhookVerifier;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;

@Path("/v1/webhooks/gocardless")
public class WebhookGoCardlessResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookGoCardlessResource.class);

    private final GoCardlessWebhookVerifier goCardlessWebhookVerifier;
    private final GoCardlessWebhookParser goCardlessWebhookParser;
    private final WebhookGoCardlessService webhookGoCardlessService;

    @Inject
    public WebhookGoCardlessResource(GoCardlessWebhookVerifier goCardlessWebhookVerifier, GoCardlessWebhookParser goCardlessWebhookParser, WebhookGoCardlessService webhookGoCardlessService) {
        this.goCardlessWebhookVerifier = goCardlessWebhookVerifier;
        this.goCardlessWebhookParser = goCardlessWebhookParser;
        this.webhookGoCardlessService = webhookGoCardlessService;
    }

    @POST
    @Timed
    public Response handleWebhook(@HeaderParam("Webhook-Signature") String webhookSignature,
                                  String body) {
        goCardlessWebhookVerifier.verify(body, webhookSignature);
        List<GoCardlessEvent> events = goCardlessWebhookParser.parse(body);
        LOGGER.info("Received valid webhook from GoCardless, containing {} events", events.size());
        webhookGoCardlessService.processEvents(events);
        return Response.status(OK).build();
    }

}
