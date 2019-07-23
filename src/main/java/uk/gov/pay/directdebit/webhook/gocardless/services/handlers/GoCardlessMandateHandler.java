package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoMandateIdException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.services.GoCardlessEventService;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdateService;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.services.PaymentService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class GoCardlessMandateHandler extends GoCardlessHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessMandateHandler.class);
    private final MandateStateUpdateService mandateStateUpdateService;
    private final MandateQueryService mandateQueryService;

    @Inject
    public GoCardlessMandateHandler(PaymentService paymentService,
                                    GoCardlessEventService goCardlessService,
                                    MandateStateUpdateService mandateStateUpdateService,
                                    MandateQueryService mandateQueryService) {
        super(paymentService, goCardlessService);
        this.mandateStateUpdateService = mandateStateUpdateService;
        this.mandateQueryService = mandateQueryService;
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

    private Map<GoCardlessAction, Consumer<Mandate>> getHandledActions() {
        return ImmutableMap.of(
                GoCardlessMandateAction.CANCELLED, (Mandate mandate) -> {
                    paymentService.findPaymentsForMandate(mandate.getExternalId()).stream()
                            .filter(payment -> paymentService.findPaymentSubmittedEventFor(payment).isEmpty())
                            .forEach(paymentService::paymentFailedWithoutEmailFor);

                    mandateStateUpdateService.mandateCancelledFor(mandate);
                },
                GoCardlessMandateAction.FAILED, (Mandate mandate) -> {
                    paymentService
                            .findPaymentsForMandate(mandate.getExternalId())
                            .forEach(paymentService::paymentFailedWithoutEmailFor);

                    mandateStateUpdateService.mandateFailedFor(mandate);
                });
    }

    @Override
    protected Optional<DirectDebitEvent> process(GoCardlessEvent event) {
        Optional.ofNullable(GoCardlessMandateAction.fromString(event.getAction()))
                .map(action -> getHandledActions().get(action))
                .ifPresent((handledAction -> {
                    Mandate mandate = mandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                            event.getLinksMandate().orElseThrow(() -> new GoCardlessEventHasNoMandateIdException(event.getGoCardlessEventId())),
                            event.getLinksOrganisation());

                    if (isValidOrganisation(mandate, event)) {
                        handledAction.accept(mandate);
                    } else {
                        LOGGER.info("Event from GoCardless with goCardlessEventId: {} has unrecognised organisation: {}",
                                event.getGoCardlessEventId(), event.getLinksOrganisation());
                    }
                }));
        
        // TODO: this shouldn't return anything if it stays around, currently return empty as required by interface
        return Optional.empty();
    }

    private boolean isValidOrganisation(Mandate mandate, GoCardlessEvent event) {
        return mandate.getGatewayAccount().getOrganisation()
                .map(organisationIdentifier -> organisationIdentifier.equals(event.getLinksOrganisation()))
                // TODO: replace true with false after going live. kept now for backwards compatibility with GetDirectDebitEventsIT
                .orElse(true);
    }
}
