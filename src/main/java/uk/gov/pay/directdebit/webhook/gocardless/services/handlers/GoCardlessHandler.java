package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.model.Event;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.Optional;

public abstract class GoCardlessHandler implements GoCardlessActionHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessHandler.class);

    protected TransactionService transactionService;
    GoCardlessService goCardlessService;

    protected abstract Optional<Event> process(GoCardlessEvent event);

    GoCardlessHandler(TransactionService transactionService,
                      GoCardlessService goCardlessService) {
        this.transactionService = transactionService;
        this.goCardlessService = goCardlessService;
    }

    public void handle(GoCardlessEvent event) {
        process(event).ifPresent((paymentRequestEvent) -> {
            event.setDirectDebitEventId(paymentRequestEvent.getId());
            goCardlessService.updateInternalEventId(event);
            LOGGER.info("handled gocardless event with id: {}, resource type: {}", event.getDirectDebitEventId(), event.getResourceType().toString());
        });
    }
}
