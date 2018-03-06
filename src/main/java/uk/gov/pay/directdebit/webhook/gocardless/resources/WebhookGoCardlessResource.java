package uk.gov.pay.directdebit.webhook.gocardless.resources;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.webhook.gocardless.api.WebhookParser;
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
    private static final Logger LOGGER = PayLoggerFactory.getLogger(WebhookGoCardlessResource.class);

    private final WebhookVerifier webhookVerifier;
    private final WebhookParser webhookParser;
    private final WebhookGoCardlessService webhookGoCardlessService;

    @Inject
    public WebhookGoCardlessResource(WebhookVerifier webhookVerifier, WebhookParser webhookParser, WebhookGoCardlessService webhookGoCardlessService) {
        this.webhookVerifier = webhookVerifier;
        this.webhookParser = webhookParser;
        this.webhookGoCardlessService = webhookGoCardlessService;
    }

    @POST
    public Response handleWebhook(@HeaderParam("Webhook-Signature") String webhookSignature,
                                  String body) {
        webhookVerifier.verify(body, webhookSignature);
        List<GoCardlessEvent> events = webhookParser.parse(body);
        LOGGER.info("Received valid webhook from GoCardless, containing {} events", events.size());
        webhookGoCardlessService.handleEvents(events);
        return Response.status(OK).build();
    }

}
