package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.services.GoCardlessEventService;
import uk.gov.pay.directdebit.payments.services.PaymentService;

public abstract class GoCardlessHandler implements GoCardlessActionHandler {
    PaymentService paymentService;
    
    GoCardlessEventService goCardlessService;

    protected abstract void process(GoCardlessEvent event);

    GoCardlessHandler(PaymentService paymentService,
                      GoCardlessEventService goCardlessService) {
        this.paymentService = paymentService;
        this.goCardlessService = goCardlessService;
    }

    public void handle(GoCardlessEvent event) {
        process(event);
    }
}
