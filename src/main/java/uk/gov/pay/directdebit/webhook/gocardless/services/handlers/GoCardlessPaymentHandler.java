package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.inject.Inject;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.exception.InvalidStateException;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

public class GoCardlessPaymentHandler extends GoCardlessHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessPaymentHandler.class);

    @Inject
    public GoCardlessPaymentHandler(TransactionService transactionService,
            GoCardlessService goCardlessService) {
        super(transactionService, goCardlessService);
    }

    /**
     * GoCardless payment actions
     *
     * @see <a href="https://developer.gocardless.com/api-reference/#events-payment-actions">https://developer.gocardless.com/api-reference/#events-payment-actions</a>
     */
    public enum GoCardlessPaymentAction implements GoCardlessAction {
        CREATED, SUBMITTED, CONFIRMED, FAILED, PAID_OUT, PAID;

        public static GoCardlessPaymentAction fromString(String type) {
            for (GoCardlessPaymentAction typeEnum : values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    return typeEnum;
                }
            }
            LOGGER.error("Webhook from GoCardless with unrecognised payment action: {}", type);
            return null;
        }
    }

    @Override
    protected Optional<DirectDebitEvent> process(GoCardlessEvent event) {
        return Optional.ofNullable(GoCardlessPaymentAction.fromString(event.getAction()))
                .map((action) -> getHandledActions().get(action))
                .map((handledAction) -> {
                    GoCardlessPayment goCardlessPayment = goCardlessService.findPaymentForEvent(event);
                    Transaction transaction = transactionService.findTransaction(goCardlessPayment.getTransactionId());
                    return handledAction.apply(transaction);
                });
    }

    private Map<GoCardlessAction, Function<Transaction, DirectDebitEvent>> getHandledActions() {
        return ImmutableMap.<GoCardlessAction, Function<Transaction, DirectDebitEvent>>builder()
                .put(GoCardlessPaymentAction.CREATED, transactionService::paymentAcknowledgedFor)
                .put(GoCardlessPaymentAction.SUBMITTED, transactionService::paymentSubmittedFor)
                .put(GoCardlessPaymentAction.CONFIRMED, (Transaction transaction) ->
                        transactionService.findPaymentSubmittedEventFor(transaction)
                                .orElseThrow(() -> new InvalidStateException("Could not find payment submitted event for transaction with id: " + transaction.getExternalId())))
                .put(GoCardlessPaymentAction.FAILED, (Transaction transaction) -> transactionService.paymentFailedWithEmailFor(transaction))
                .put(GoCardlessPaymentAction.PAID_OUT, transactionService::paymentPaidOutFor)
                .put(GoCardlessPaymentAction.PAID, transactionService::payoutPaidFor)
                .build();
    }
}
