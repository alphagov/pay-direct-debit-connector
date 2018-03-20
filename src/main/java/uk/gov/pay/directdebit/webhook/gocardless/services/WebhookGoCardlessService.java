package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessActionHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import javax.inject.Inject;
import java.util.List;

public class WebhookGoCardlessService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(WebhookGoCardlessService.class);

    private final GoCardlessService goCardlessService;
    private final GoCardlessPaymentHandler goCardlessPaymentHandler;
    private final GoCardlessMandateHandler goCardlessMandateHandler;

    @Inject
    public WebhookGoCardlessService(GoCardlessService goCardlessService, TransactionService transactionService, PayerService payerService) {
        this.goCardlessService = goCardlessService;
        this.goCardlessPaymentHandler = new GoCardlessPaymentHandler(transactionService, goCardlessService);
        this.goCardlessMandateHandler = new GoCardlessMandateHandler(transactionService, goCardlessService, payerService);
    }

    public void handleEvents(List<GoCardlessEvent> events) {
        events.forEach(event -> {
            GoCardlessActionHandler handler = getHandlerFor(event.getResourceType());
            LOGGER.info("About to handle event of type: {}, action: {}, resource id: {}",
                    event.getResourceType(),
                    event.getAction(),
                    event.getResourceId());
            handler.handle(event);

        });
    }

    private GoCardlessActionHandler getHandlerFor(GoCardlessResourceType goCardlessResourceType) {
        switch (goCardlessResourceType) {
            case PAYMENTS:
                return goCardlessPaymentHandler;
            case MANDATES:
                return goCardlessMandateHandler;
            default:
                return goCardlessService::storeEvent;
        }
    }
}
