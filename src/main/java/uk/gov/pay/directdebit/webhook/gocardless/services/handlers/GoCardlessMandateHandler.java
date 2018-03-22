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
       return GoCardlessMandateAction.fromString(action);
    }

    @Override
    protected Transaction getTransactionForEvent(GoCardlessEvent event) {
        GoCardlessMandate goCardlessMandate = goCardlessService.findMandateForEvent(event);
        return transactionService.findTransactionForMandateId(goCardlessMandate.getMandateId());
    }

    public enum GoCardlessMandateAction implements GoCardlessAction {
        CREATED, SUBMITTED,
        ACTIVE {
            @Override
            public PaymentRequestEvent process(TransactionService transactionService, Transaction transaction) {
                return transactionService.mandateActiveFor(transaction);
            }
        };

        @Override
        public PaymentRequestEvent process(TransactionService transactionService, Transaction transaction) {
            return transactionService.findMandatePendingEventFor(transaction)
                    .orElseGet(() -> transactionService.mandatePendingFor(transaction));
        }

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
