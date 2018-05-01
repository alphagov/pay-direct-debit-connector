package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.exception.InvalidStateException;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class GoCardlessPaymentHandler extends GoCardlessHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessPaymentHandler.class);

    @Inject
    public GoCardlessPaymentHandler(TransactionService transactionService, GoCardlessService goCardlessService) {
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
                    Transaction transaction = transactionService.findTransactionFor(goCardlessPayment.getTransactionId());
                    return handledAction.apply(transaction);
                });
    }

    @Override
    protected Map<GoCardlessAction, Function<Transaction, PaymentRequestEvent>> getHandledActions() {
        return ImmutableMap.<GoCardlessAction, Function<Transaction, PaymentRequestEvent>>builder()
                .put(GoCardlessPaymentAction.CREATED, transactionService::paymentPendingFor)
                .put(GoCardlessPaymentAction.SUBMITTED, transactionService::paymentSubmittedFor)
                .put(GoCardlessPaymentAction.CONFIRMED, (Transaction transaction) ->
                        transactionService.findPaymentSubmittedEventFor(transaction)
                                .orElseThrow(() -> new InvalidStateException("Could not find payment submitted event for payment request with id: " + transaction.getPaymentRequest().getExternalId())))
                .put(GoCardlessPaymentAction.FAILED, transactionService::paymentFailedFor)
                .put(GoCardlessPaymentAction.PAID_OUT, transactionService::paymentPaidOutFor)
                .put(GoCardlessPaymentAction.PAID, transactionService::payoutPaidFor)
                .build();
    }
}
