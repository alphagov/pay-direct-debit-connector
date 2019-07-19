package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.services.GoCardlessEventService;
import uk.gov.pay.directdebit.payments.services.PaymentService;

import java.util.Optional;

public abstract class GoCardlessHandler implements GoCardlessActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessHandler.class);

    protected PaymentService paymentService;
    GoCardlessEventService goCardlessService;

    protected abstract Optional<DirectDebitEvent> process(GoCardlessEvent event);

    GoCardlessHandler(PaymentService paymentService,
                      GoCardlessEventService goCardlessService) {
        this.paymentService = paymentService;
        this.goCardlessService = goCardlessService;
    }

    public void handle(GoCardlessEvent event) {
        process(event).ifPresent((directDebitEvent) -> {
            event.setInternalEventId(directDebitEvent.getId());
            goCardlessService.updateInternalEventId(event);
            LOGGER.info("handled gocardless event with id: {}, resource type: {}", event.getInternalEventId(), event.getResourceType().toString());
        });
    }
}
