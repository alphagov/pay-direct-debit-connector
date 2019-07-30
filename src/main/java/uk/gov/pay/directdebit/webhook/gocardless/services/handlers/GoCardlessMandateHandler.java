package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoMandateIdException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessMandateAction;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Consumer;

public class GoCardlessMandateHandler {

    private final MandateQueryService mandateQueryService;
    private final UserNotificationService userNotificationService;

    @Inject
    public GoCardlessMandateHandler(MandateQueryService mandateQueryService,
                                    UserNotificationService userNotificationService) {
        this.mandateQueryService = mandateQueryService;
        this.userNotificationService = userNotificationService;
    }

    private Map<GoCardlessMandateAction, Consumer<Mandate>> getHandledActions() {
        return Map.of(
                GoCardlessMandateAction.CANCELLED, userNotificationService::sendMandateCancelledEmailFor,
                GoCardlessMandateAction.FAILED, userNotificationService::sendMandateFailedEmailFor);
    }

    public void handle(GoCardlessEvent event, GoCardlessMandateAction action) {
        Mandate mandate = mandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                event.getLinksMandate().orElseThrow(() -> new GoCardlessEventHasNoMandateIdException(event.getGoCardlessEventId())),
                event.getLinksOrganisation());
        getHandledActions().getOrDefault(action, p -> {}).accept(mandate);
    }
}
