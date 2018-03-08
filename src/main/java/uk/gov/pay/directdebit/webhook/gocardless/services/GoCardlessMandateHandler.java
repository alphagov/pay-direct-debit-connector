package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

public class GoCardlessMandateHandler implements GoCardlessActionHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessMandateHandler.class);

    public enum GoCardlessMandateAction implements GoCardlessAction {
        CREATED {
            @Override
            public PaymentRequestEvent changeTransactionState(TransactionService transactionService, Transaction transaction) {
                return transactionService.mandateCreatedFor(transaction);
            }
        };


        public static GoCardlessMandateAction fromString(String type) {
            for (GoCardlessMandateAction typeEnum : GoCardlessMandateAction.values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    return typeEnum;
                }
            }
            LOGGER.warn("Received webhook from gocardless with unhandled mandate action: {}", type);
            return null;
        }
    }

    private final TransactionService transactionService;
    private final GoCardlessService goCardlessService;
    public GoCardlessMandateHandler(TransactionService transactionService, GoCardlessService goCardlessService) {
        this.transactionService = transactionService;
        this.goCardlessService = goCardlessService;
    }

    public void handle(GoCardlessEvent event) {
        GoCardlessAction goCardlessAction = GoCardlessMandateAction.fromString(event.getAction());
        if (goCardlessAction != null) {
            GoCardlessMandate goCardlessMandate = goCardlessService.findMandateForEvent(event);
            Transaction transaction = transactionService.findTransactionForMandateId(goCardlessMandate.getMandateId());
            PaymentRequestEvent paymentRequestEvent = goCardlessAction.changeTransactionState(transactionService, transaction);
            event.setPaymentRequestEventId(paymentRequestEvent.getId());
            LOGGER.info("handled gocardless mandate event with id {} ", event.getEventId());
        }
        goCardlessService.storeEvent(event);
    }
}
