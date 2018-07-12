package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.payments.services.GoCardlessEventService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessActionHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import javax.inject.Inject;
import java.util.List;

public class WebhookGoCardlessService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(WebhookGoCardlessService.class);

    private final GoCardlessEventService goCardlessService;
    private final GoCardlessPaymentHandler goCardlessPaymentHandler;
    private final GoCardlessMandateHandler goCardlessMandateHandler;

    @Inject
    public WebhookGoCardlessService(GoCardlessEventService goCardlessService,
                                    GoCardlessPaymentHandler goCardlessPaymentHandler,
                                    GoCardlessMandateHandler goCardlessMandateHandler) {
        this.goCardlessService = goCardlessService;
        this.goCardlessPaymentHandler = goCardlessPaymentHandler;
        this.goCardlessMandateHandler = goCardlessMandateHandler;
    }

    public void handleEvents(List<GoCardlessEvent> events) {
        events.forEach(goCardlessService::storeEvent);
        events.forEach(event -> {
            GoCardlessActionHandler handler = getHandlerFor(event.getResourceType());
            LOGGER.info("About to handle event of type: {}, action: {}, resource id: {}",
                    event.getResourceType(),
                    event.getAction(),
                    event.getResourceId());
            handler.handle(event);
        });
    }

    private void logUnknownResourceTypeForEvent(GoCardlessEvent event) {
        LOGGER.info("unhandled resource type for event with id {} ", event.getEventId());
    }

    private GoCardlessActionHandler getHandlerFor(GoCardlessResourceType goCardlessResourceType) {
        switch (goCardlessResourceType) {
            case PAYMENTS:
            case PAYOUTS:
                return goCardlessPaymentHandler;
            case MANDATES:
                return goCardlessMandateHandler;
            default:
                return this::logUnknownResourceTypeForEvent;
        }
    }
}
