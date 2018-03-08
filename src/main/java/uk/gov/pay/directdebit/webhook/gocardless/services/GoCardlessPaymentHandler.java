package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

public class GoCardlessPaymentHandler implements GoCardlessActionHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessPaymentHandler.class);

    public enum GoCardlessPaymentAction implements GoCardlessAction {
        //todo add more supported payment actions (https://developer.gocardless.com/api-reference/#events-payment-actions)
        PAID_OUT {
            @Override
            public PaymentRequestEvent changeTransactionState(TransactionService transactionService, Transaction transaction) {
                return transactionService.paidOutFor(transaction);
            }
        };

        private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessPaymentAction.class);

        public static GoCardlessPaymentAction fromString(String type) {
            for (GoCardlessPaymentAction typeEnum : GoCardlessPaymentAction.values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    return typeEnum;
                }
            }
            LOGGER.warn("Received webhook from gocardless with unhandled payment action: {}", type);
            return null;
        }
    }

    private final TransactionService transactionService;
    private final GoCardlessService goCardlessService;

    public GoCardlessPaymentHandler(TransactionService transactionService, GoCardlessService goCardlessService) {
        this.transactionService = transactionService;
        this.goCardlessService = goCardlessService;
    }

    public void handle(GoCardlessEvent event) {
        GoCardlessAction goCardlessAction = GoCardlessPaymentAction.fromString(event.getAction());
        if (goCardlessAction != null) {
            GoCardlessPayment goCardlessPayment = goCardlessService.findPaymentForEvent(event);
            Transaction transaction = transactionService.findTransactionFor(goCardlessPayment.getTransactionId());
            PaymentRequestEvent paymentRequestEvent = goCardlessAction.changeTransactionState(transactionService, transaction);
            event.setPaymentRequestEventId(paymentRequestEvent.getId());
            LOGGER.info("handled gocardless payment event with id {} ", event.getEventId());
        }
        goCardlessService.storeEvent(event);
    }

}
