package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoMandateIdException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class GoCardlessMandateHandler implements GoCardlessActionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessMandateHandler.class);
    private final MandateQueryService mandateQueryService;
    private final UserNotificationService userNotificationService;

    @Inject
    public GoCardlessMandateHandler(MandateQueryService mandateQueryService,
                                    UserNotificationService userNotificationService) {
        this.mandateQueryService = mandateQueryService;
        this.userNotificationService = userNotificationService;
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
                GoCardlessMandateAction.CANCELLED, userNotificationService::sendMandateCancelledEmailFor,
                GoCardlessMandateAction.FAILED, userNotificationService::sendMandateFailedEmailFor);
    }

    @Override
    public void handle(GoCardlessEvent event) {
        Optional.ofNullable(GoCardlessMandateAction.fromString(event.getAction()))
                .map(action -> getHandledActions().get(action))
                .ifPresent((handledAction -> {
                    Mandate mandate = mandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                            event.getLinksMandate().orElseThrow(() -> new GoCardlessEventHasNoMandateIdException(event.getGoCardlessEventId())),
                            event.getLinksOrganisation());

                    handledAction.accept(mandate);
                }));
    }
}
