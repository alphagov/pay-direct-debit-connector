package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

public class GoCardlessPaymentHandler extends GoCardlessHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessPaymentHandler.class);

    public GoCardlessPaymentHandler(TransactionService transactionService, GoCardlessService goCardlessService) {
        super(transactionService, goCardlessService);
    }

    public enum GoCardlessPaymentAction implements GoCardlessAction {
        CREATED {
            @Override
            public PaymentRequestEvent process(TransactionService transactionService, Transaction transaction) {
                return transactionService.paymentPendingFor(transaction);
            }
        },
        SUBMITTED,
        CONFIRMED,
        PAID_OUT {
            @Override
            public PaymentRequestEvent process(TransactionService transactionService, Transaction transaction) {
                return transactionService.paymentPaidOutFor(transaction);
            }
        };

        @Override
        public PaymentRequestEvent process(TransactionService transactionService, Transaction transaction) {
            return transactionService.findPaymentPendingEventFor(transaction);
        }

        public static GoCardlessPaymentAction fromString(String type) {
            for (GoCardlessPaymentAction typeEnum : values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    return typeEnum;
                }
            }
            LOGGER.warn("Webhook from GoCardless with unrecognised payment action: {}", type);
            return null;
        }
    }

    @Override
    protected GoCardlessAction parseAction(String action) {
        return GoCardlessPaymentAction.fromString(action);
    }

    @Override
    protected Transaction getTransactionForEvent(GoCardlessEvent event) {
        GoCardlessPayment goCardlessPayment = goCardlessService.findPaymentForEvent(event);
        return transactionService.findTransactionFor(goCardlessPayment.getTransactionId());
    }
}
