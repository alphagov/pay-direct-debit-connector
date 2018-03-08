package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import javax.inject.Inject;
import java.util.List;

public class WebhookGoCardlessService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(WebhookGoCardlessService.class);
    private final TransactionService transactionService;
    private final GoCardlessService goCardlessService;

    @Inject
    public WebhookGoCardlessService(GoCardlessService goCardlessService, TransactionService transactionService) {
        this.goCardlessService = goCardlessService;
        this.transactionService = transactionService;
    }

    public void handleEvents(List<GoCardlessEvent> events) {
        events.forEach(event -> {
            GoCardlessActionHandler handler = getHandlerFor(event.getResourceType());
            if (handler != null) {
                LOGGER.info("About to handle event of type: {}, action: {}, resource id: {}",
                        event.getResourceType(),
                        event.getAction(),
                        event.getResourceId());
                handler.handle(event);
            }

        });
    }

    private GoCardlessActionHandler getHandlerFor(GoCardlessResourceType goCardlessResourceType) {
        switch (goCardlessResourceType) {
            case PAYMENTS:
                return new GoCardlessPaymentHandler(transactionService, goCardlessService);
            case MANDATES:
                return new GoCardlessMandateHandler(transactionService, goCardlessService);
                default:
                    return goCardlessService::storeEvent;
        }
    }
}
