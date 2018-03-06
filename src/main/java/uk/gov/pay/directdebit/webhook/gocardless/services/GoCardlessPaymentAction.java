package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

public enum GoCardlessPaymentAction implements GoCardlessAction {
    //todo add more supported payment actions (https://developer.gocardless.com/api-reference/#events-payment-actions)
    PAID_OUT {
        @Override
        public PaymentRequestEvent changeChargeState(TransactionService transactionService, Transaction transaction) {
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
