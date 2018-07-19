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
import uk.gov.pay.directdebit.mandate.services.MandateServiceFactory;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.services.DirectDebitEventService;
import uk.gov.pay.directdebit.payments.services.GoCardlessEventService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

public class GoCardlessMandateHandler extends GoCardlessHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessMandateHandler.class);
    private final MandateServiceFactory mandateServiceFactory;
    private final DirectDebitEventService directDebitEventService;
    @Inject
    public GoCardlessMandateHandler(TransactionService transactionService,
            GoCardlessEventService goCardlessService,
            MandateServiceFactory mandateServiceFactory,
            DirectDebitEventService directDebitEventService) {
        super(transactionService, goCardlessService);
        this.mandateServiceFactory = mandateServiceFactory;
        this.directDebitEventService = directDebitEventService;
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
                GoCardlessMandateAction.ACTIVE, mandateServiceFactory.getMandateStateUpdateService()::mandateActiveFor,
                GoCardlessMandateAction.CANCELLED, (Mandate mandate) -> {
                    transactionService.findTransactionsForMandate(mandate.getExternalId()).stream()
                            .filter(transaction -> !transactionService.findPaymentSubmittedEventFor(transaction).isPresent())
                            .forEach(transactionService::paymentFailedWithoutEmailFor);

                    return mandateServiceFactory.getMandateStateUpdateService().mandateCancelledFor(mandate);
                },
                GoCardlessMandateAction.FAILED, (Mandate mandate) -> {
                    transactionService
                            .findTransactionsForMandate(mandate.getExternalId())
                            .forEach(transactionService::paymentFailedWithoutEmailFor);

                    return mandateServiceFactory.getMandateStateUpdateService().mandateFailedFor(mandate);
                });
    }

    @Override
    protected Optional<DirectDebitEvent> process(GoCardlessEvent event) {
        return Optional.ofNullable(GoCardlessMandateAction.fromString(event.getAction()))
                .map((action) -> getHandledActions().get(action))
                .map((handledAction -> {
                    GoCardlessMandate goCardlessMandate = goCardlessService.findGoCardlessMandateForEvent(event);
                    Mandate mandate = mandateServiceFactory.getMandateQueryService().findById(goCardlessMandate.getMandateId());
                    if (isValidOrganisation(mandate, event)) {
                        return handledAction.apply(mandate);
                    } else {
                        LOGGER.info("Event from GoCardless with goCardlessEventId: {} has unrecognised organisation: {}",
                                event.getGoCardlessEventId(), event.getOrganisationIdentifier());
                        return null;
                    }
                }));
    }

    private DirectDebitEvent findMandatePendingEventOrInsertOneIfItDoesNotExist(Mandate mandate) {
        return directDebitEventService.findBy(mandate.getId(), Type.MANDATE, SupportedEvent.MANDATE_PENDING)
                .orElseGet(() -> mandateServiceFactory.getMandateStateUpdateService().mandatePendingFor(mandate));
    }

    private boolean isValidOrganisation(Mandate mandate, GoCardlessEvent event) {
        return mandate.getGatewayAccount().getOrganisation()
                .map(organisationIdentifier -> organisationIdentifier.equals(event.getOrganisationIdentifier()))
                .orElse(false);
    }
}
