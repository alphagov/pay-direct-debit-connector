package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.services.GoCardlessEventService;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdater;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdAndOrganisationId;
import uk.gov.pay.directdebit.events.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.payments.services.gocardless.GoCardlessPaymentStateUpdater;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessActionHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;

public class WebhookGoCardlessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookGoCardlessService.class);

    private final GoCardlessEventService goCardlessService;
    private final GoCardlessPaymentHandler goCardlessPaymentHandler;
    private final GoCardlessMandateHandler goCardlessMandateHandler;
    private final MandateStateUpdater mandateStateUpdater;
    private final GoCardlessPaymentStateUpdater goCardlessPaymentStateUpdater;
    private final MandateQueryService mandateQueryService;

    @Inject
    WebhookGoCardlessService(GoCardlessEventService goCardlessService,
                             GoCardlessPaymentHandler goCardlessPaymentHandler,
                             GoCardlessMandateHandler goCardlessMandateHandler,
                             MandateStateUpdater mandateStateUpdater,
                             GoCardlessPaymentStateUpdater goCardlessPaymentStateUpdater,
                             MandateQueryService mandateQueryService) {
        this.goCardlessService = goCardlessService;
        this.goCardlessPaymentHandler = goCardlessPaymentHandler;
        this.goCardlessMandateHandler = goCardlessMandateHandler;
        this.mandateStateUpdater = mandateStateUpdater;
        this.goCardlessPaymentStateUpdater = goCardlessPaymentStateUpdater;
        this.mandateQueryService = mandateQueryService;
    }

    public void handleEvents(List<GoCardlessEvent> events) {
        events.forEach(goCardlessService::storeEvent);

        Map<GoCardlessResourceType, List<GoCardlessEvent>> eventsGroupedByResourceType = events.stream().collect(groupingBy(GoCardlessEvent::getResourceType));
        updateStateForMandateEvents(eventsGroupedByResourceType.getOrDefault(GoCardlessResourceType.MANDATES, Collections.emptyList()));
        updateStateForPaymentEvents(eventsGroupedByResourceType.getOrDefault(GoCardlessResourceType.PAYMENTS, Collections.emptyList()));

        events.forEach(event -> {
            GoCardlessActionHandler handler = getHandlerFor(event.getResourceType());
            LOGGER.info("About to handle event of type: {}, action: {}, resource id: {}",
                    event.getResourceType(),
                    event.getAction(),
                    event.getResourceId());
            handler.handle(event);
        });
    }

    private void updateStateForMandateEvents(List<GoCardlessEvent> eventsThatAffectMandates) {
        eventsThatAffectMandates.stream()
                .map(this::toGoCardlessMandateIdAndOrganisationId)
                .flatMap(Optional::stream)
                .distinct()
                .map(this::getMandate)
                .flatMap(Optional::stream)
                .forEach(mandateStateUpdater::updateState);
    }

    private Optional<Pair<GoCardlessMandateId, GoCardlessOrganisationId>> toGoCardlessMandateIdAndOrganisationId(GoCardlessEvent goCardlessEvent) {
        var goCardlessMandateIdAndOrganisationId = goCardlessEvent.getLinksMandate()
                .map(mandateId -> Pair.of(mandateId, goCardlessEvent.getLinksOrganisation()))
                .orElseGet(() -> {
                    LOGGER.error("GoCardless event " + goCardlessEvent.getGoCardlessEventId() + " has resource_type mandate but no links.mandate");
                    return null;
                });
        return Optional.ofNullable(goCardlessMandateIdAndOrganisationId);
    }

    private Optional<Mandate> getMandate(Pair<GoCardlessMandateId, GoCardlessOrganisationId> goCardlessMandateBusinessKey) {
        GoCardlessMandateId goCardlessMandateId = goCardlessMandateBusinessKey.getLeft();
        GoCardlessOrganisationId goCardlessOrganisationId = goCardlessMandateBusinessKey.getRight();
        try {
            Mandate mandate = mandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                    goCardlessMandateId,
                    goCardlessOrganisationId);

            return Optional.of(mandate);
        } catch (MandateNotFoundException e) {
            LOGGER.error(String.format("Could not update status of GoCardless mandate %s for organisation %s because the mandate was not found",
                    goCardlessMandateId,
                    goCardlessOrganisationId));
        }

        return Optional.empty();
    }

    private void updateStateForPaymentEvents(List<GoCardlessEvent> eventsThatAffectPayments) {
        eventsThatAffectPayments.stream()
                .map(WebhookGoCardlessService::toGoCardlessPaymentIdAndOrganisationId)
                .flatMap(Optional::stream)
                .distinct()
                .forEach(goCardlessPaymentStateUpdater::updateState);
    }

    private static Optional<GoCardlessPaymentIdAndOrganisationId> toGoCardlessPaymentIdAndOrganisationId(GoCardlessEvent goCardlessEvent) {
        var goCardlessPaymentIdAndOrganisationId = goCardlessEvent.getLinksPayment()
                .map(paymentId -> new GoCardlessPaymentIdAndOrganisationId(paymentId, goCardlessEvent.getLinksOrganisation()))
                .orElseGet(() -> {
                    LOGGER.error("GoCardless event " + goCardlessEvent.getGoCardlessEventId() + " has resource_type payment but no links.payment");
                    return null;
                });
        return Optional.ofNullable(goCardlessPaymentIdAndOrganisationId);
    }

    private void logUnknownResourceTypeForEvent(GoCardlessEvent event) {
        LOGGER.info("unhandled resource type for event with id {} ", event.getInternalEventId());
    }

    private GoCardlessActionHandler getHandlerFor(GoCardlessResourceType goCardlessResourceType) {
        switch (goCardlessResourceType) {
            case PAYMENTS:
            case PAYOUTS:
                return goCardlessPaymentHandler;
            case MANDATES:
                return goCardlessMandateHandler;
            default:
                return this::logUnknownResourceTypeForEvent;
        }
    }
}
