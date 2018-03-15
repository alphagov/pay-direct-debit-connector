package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

public class GoCardlessMandateHandler extends GoCardlessHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessMandateHandler.class);

    public GoCardlessMandateHandler(TransactionService transactionService, GoCardlessService goCardlessService) {
        super(transactionService, goCardlessService);
    }

    @Override
    protected GoCardlessAction parseAction(String action) {
        GoCardlessMandateAction mandateAction = GoCardlessMandateAction.fromString(action);
        if (mandateAction != null) {
            return (transactionService, transaction) -> {
                PaymentRequestEvent paymentPendingEvent = null;
                if (mandateAction != null) {
                    paymentPendingEvent = transactionService.findPaymentPendingEventFor(transaction);
                }
                return paymentPendingEvent;
            };
        }
        return null;
    }

    @Override
    protected Transaction getTransactionForEvent(GoCardlessEvent event) {
        GoCardlessMandate goCardlessMandate = goCardlessService.findMandateForEvent(event);
        return transactionService.findTransactionForMandateId(goCardlessMandate.getMandateId());
    }

    public enum GoCardlessMandateAction {
        CREATED, SUBMITTED, ACTIVE;

        public static GoCardlessMandateAction fromString(String type) {
            for (GoCardlessMandateAction typeEnum : GoCardlessMandateAction.values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    LOGGER.info("Webhook from GoCardless with mandate action: {}", type);
                    return typeEnum;
                }
            }
            LOGGER.warn("Webhook from GoCardless with unrecognised mandate action: {}", type);
            return null;
        }
    }
}
