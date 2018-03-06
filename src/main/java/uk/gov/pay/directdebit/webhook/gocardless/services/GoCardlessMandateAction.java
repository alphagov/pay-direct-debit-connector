package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

public enum GoCardlessMandateAction implements GoCardlessAction {
    CREATED {
        @Override
        public PaymentRequestEvent changeChargeState(TransactionService transactionService, Transaction transaction) {
            return transactionService.mandateCreatedFor(transaction);
        }
    };

    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessMandateAction.class);

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
