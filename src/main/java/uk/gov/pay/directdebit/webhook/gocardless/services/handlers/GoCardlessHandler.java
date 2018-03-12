package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

public abstract class GoCardlessHandler implements GoCardlessActionHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessHandler.class);
    protected TransactionService transactionService;
    GoCardlessService goCardlessService;

    protected abstract GoCardlessAction parseAction(String action);
    protected abstract Transaction getTransactionForEvent(GoCardlessEvent event);


    public GoCardlessHandler(TransactionService transactionService, GoCardlessService goCardlessService) {
        this.transactionService = transactionService;
        this.goCardlessService = goCardlessService;
    }

    public void handle(GoCardlessEvent event) {
        GoCardlessAction goCardlessAction = parseAction(event.getAction());
        if (goCardlessAction != null) {
            Transaction transaction = getTransactionForEvent(event);
            PaymentRequestEvent paymentRequestEvent = goCardlessAction.changeTransactionState(transactionService, transaction);
            event.setPaymentRequestEventId(paymentRequestEvent.getId());
            LOGGER.info("handled gocardless event with id: {}, resource type: {}", event.getEventId(), event.getResourceType().toString());
        }
        goCardlessService.storeEvent(event);
    }
}
