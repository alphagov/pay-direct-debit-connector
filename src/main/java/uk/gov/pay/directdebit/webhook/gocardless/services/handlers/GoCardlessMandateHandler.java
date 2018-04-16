package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.gocardless.services.MandateService;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
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

public class GoCardlessMandateHandler extends GoCardlessHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessMandateHandler.class);
    private final PayerService payerService;

    @Inject
    public GoCardlessMandateHandler(TransactionService transactionService, GoCardlessService goCardlessService, PayerService payerService) {
        super(transactionService, goCardlessService);
        this.payerService = payerService;
    }

    @Override
    protected Map<GoCardlessAction, Function<Transaction, PaymentRequestEvent>> getHandledActions() {
        return ImmutableMap.of(
                GoCardlessMandateAction.CREATED, this::findMandatePendingEventOrInsertOneIfItDoesNotExist,
                GoCardlessMandateAction.SUBMITTED, this::findMandatePendingEventOrInsertOneIfItDoesNotExist,
                GoCardlessMandateAction.ACTIVE, transactionService::mandateActiveFor,
                GoCardlessMandateAction.CANCELLED, (Transaction transaction) -> {
                    Payer payer = payerService.getPayerFor(transaction);
                    if (!transactionService.findPaymentSubmittedEventFor(transaction).isPresent()) {
                        transactionService.paymentFailedFor(transaction);
                    }
                    return transactionService.mandateCancelledFor(transaction, payer);
                },
                GoCardlessMandateAction.FAILED, (Transaction transaction) -> {
                    Payer payer = payerService.getPayerFor(transaction);
                    transactionService.mandateFailedFor(transaction, payer);
                    return transactionService.paymentFailedFor(transaction);
                });
    }
    @Override
    protected Optional<PaymentRequestEvent> process(GoCardlessEvent event) {
        return Optional.ofNullable(GoCardlessMandateAction.fromString(event.getAction()))
                .map((action) -> getHandledActions().get(action))
                .map((handledAction -> {
                    GoCardlessMandate goCardlessMandate = goCardlessService.findMandateForEvent(event);
                    Transaction transaction = transactionService.findTransactionForMandateId(goCardlessMandate.getMandateId());
                    return handledAction.apply(transaction);
                }));
    }

    private PaymentRequestEvent findMandatePendingEventOrInsertOneIfItDoesNotExist(Transaction transaction) {
        return transactionService.findMandatePendingEventFor(transaction)
                .orElseGet(() -> transactionService.mandatePendingFor(transaction));
    }

    public enum GoCardlessMandateAction implements GoCardlessAction {
        CREATED, SUBMITTED, ACTIVE, FAILED, CANCELLED;


        public static GoCardlessMandateAction fromString(String type) {
            for (GoCardlessMandateAction typeEnum : values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    LOGGER.info("Webhook from GoCardless with mandate action: {}", type);
                    return typeEnum;
                }
            }
            LOGGER.warn("Webhook from GoCardless with unhandled mandate action: {}", type);
            return null;
        }
    }
}
