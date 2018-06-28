package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.inject.Inject;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

public class GoCardlessMandateHandler extends GoCardlessHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessMandateHandler.class);
    private final MandateService mandateService;

    @Inject
    public GoCardlessMandateHandler(TransactionService transactionService,
            GoCardlessService goCardlessService,
            MandateService mandateService) {
        super(transactionService, goCardlessService);
        this.mandateService = mandateService;
    }

    /**
     * GoCardless mandate actions
     *
     * @see <a href="https://developer.gocardless.com/api-reference/#events-mandate-actions">https://developer.gocardless.com/api-reference/#events-mandate-actions</a>
     */
    public enum GoCardlessMandateAction implements GoCardlessAction {
        CREATED, SUBMITTED, ACTIVE, FAILED, CANCELLED;

        public static GoCardlessMandateAction fromString(String type) {
            for (GoCardlessMandateAction typeEnum : values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    LOGGER.info("Webhook from GoCardless with mandate action: {}", type);
                    return typeEnum;
                }
            }
            LOGGER.error("Webhook from GoCardless with unhandled mandate action: {}", type);
            return null;
        }
    }

    private Map<GoCardlessAction, Function<Mandate, DirectDebitEvent>> getHandledActions() {
        return ImmutableMap.of(
                GoCardlessMandateAction.CREATED, this::findMandatePendingEventOrInsertOneIfItDoesNotExist,
                GoCardlessMandateAction.SUBMITTED, this::findMandatePendingEventOrInsertOneIfItDoesNotExist,
                GoCardlessMandateAction.ACTIVE, mandateService::mandateActiveFor,
                GoCardlessMandateAction.CANCELLED, (Mandate mandate) -> {
                    transactionService.findTransactionsForMandate(mandate.getExternalId()).stream()
                            .filter(transaction -> !transactionService.findPaymentSubmittedEventFor(transaction).isPresent())
                            .forEach(transactionService::paymentFailedWithoutEmailFor);

                    return mandateService.mandateCancelledFor(mandate);
                },
                GoCardlessMandateAction.FAILED, (Mandate mandate) -> {
                    transactionService
                            .findTransactionsForMandate(mandate.getExternalId())
                            .forEach(transactionService::paymentFailedWithoutEmailFor);

                    return  mandateService.mandateFailedFor(mandate);
                });
    }

    @Override
    protected Optional<DirectDebitEvent> process(GoCardlessEvent event) {
        return Optional.ofNullable(GoCardlessMandateAction.fromString(event.getAction()))
                .map((action) -> getHandledActions().get(action))
                .map((handledAction -> {
                    GoCardlessMandate goCardlessMandate = goCardlessService.findGoCardlessMandateForEvent(event);
                    Mandate mandate = mandateService.findById(goCardlessMandate.getMandateId());
                    return handledAction.apply(mandate);
                }));
    }

    private DirectDebitEvent findMandatePendingEventOrInsertOneIfItDoesNotExist(Mandate mandate) {
        return mandateService.findMandatePendingEventFor(mandate)
                .orElseGet(() -> mandateService.mandatePendingFor(mandate));
    }
}
