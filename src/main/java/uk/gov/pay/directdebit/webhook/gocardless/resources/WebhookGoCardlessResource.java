package uk.gov.pay.directdebit.webhook.gocardless.resources;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.webhook.gocardless.api.GoCardlessWebhookParser;
import uk.gov.pay.directdebit.webhook.gocardless.services.WebhookGoCardlessService;
import uk.gov.pay.directdebit.webhook.gocardless.support.WebhookVerifier;

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

    private final WebhookVerifier webhookVerifier;
    private final GoCardlessWebhookParser goCardlessWebhookParser;
    private final WebhookGoCardlessService webhookGoCardlessService;

    @Inject
    public WebhookGoCardlessResource(WebhookVerifier webhookVerifier, GoCardlessWebhookParser goCardlessWebhookParser, WebhookGoCardlessService webhookGoCardlessService) {
        this.webhookVerifier = webhookVerifier;
        this.goCardlessWebhookParser = goCardlessWebhookParser;
        this.webhookGoCardlessService = webhookGoCardlessService;
    }

    @POST
    @Timed
    public Response handleWebhook(@HeaderParam("Webhook-Signature") String webhookSignature,
                                  String body) {
        webhookVerifier.verify(body, webhookSignature);
        List<GoCardlessEvent> events = goCardlessWebhookParser.parse(body);
        LOGGER.info("Received valid webhook from GoCardless, containing {} events", events.size());
        webhookGoCardlessService.handleEvents(events);
        return Response.status(OK).build();
    }

}
