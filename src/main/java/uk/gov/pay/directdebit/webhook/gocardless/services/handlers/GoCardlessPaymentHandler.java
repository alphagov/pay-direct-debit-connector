package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class GoCardlessPaymentHandler extends GoCardlessHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessPaymentHandler.class);

    public GoCardlessPaymentHandler(TransactionService transactionService, GoCardlessService goCardlessService) {
        super(transactionService, goCardlessService);
    }

    public enum GoCardlessPaymentAction implements GoCardlessAction {
        CREATED, SUBMITTED, CONFIRMED, PAID_OUT;

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
    protected Optional<PaymentRequestEvent> process(GoCardlessEvent event) {
        return Optional.ofNullable(GoCardlessPaymentAction.fromString(event.getAction()))
                .map((action) -> getHandledActions().get(action))
                .map((handledAction) -> {
                    GoCardlessPayment goCardlessPayment = goCardlessService.findPaymentForEvent(event);
                    Transaction transactionForMandateId = transactionService.findTransactionFor(goCardlessPayment.getTransactionId());
                    return handledAction.apply(transactionForMandateId);
                });
    }

    @Override
    protected Map<GoCardlessAction, Function<Transaction, PaymentRequestEvent>> getHandledActions() {
        return ImmutableMap.of(
                GoCardlessPaymentAction.CREATED, transactionService::paymentPendingFor,
                GoCardlessPaymentAction.SUBMITTED, transactionService::findPaymentPendingEventFor,
                GoCardlessPaymentAction.CONFIRMED, transactionService::findPaymentPendingEventFor,
                GoCardlessPaymentAction.PAID_OUT, transactionService::paymentPaidOutFor
        );
    }

}
