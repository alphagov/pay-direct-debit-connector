package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.events.services.GoCardlessEventService;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdater;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.payments.services.PaymentStateUpdater;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.PAYOUTS;

public class WebhookGoCardlessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookGoCardlessService.class);
    
    private static final List<GoCardlessResourceType> VALID_RESOURCE_TYPES = List.of(PAYMENTS, MANDATES, PAYOUTS);

    private final GoCardlessEventService goCardlessService;
    private final GoCardlessPaymentHandler goCardlessPaymentHandler;
    private final GoCardlessMandateHandler goCardlessMandateHandler;
    private final MandateStateUpdater mandateStateUpdater;
    private final PaymentStateUpdater paymentStateUpdater;
    private final MandateQueryService mandateQueryService;
    private final PaymentQueryService paymentQueryService;

    @Inject
    WebhookGoCardlessService(GoCardlessEventService goCardlessService,
                             GoCardlessPaymentHandler goCardlessPaymentHandler,
                             GoCardlessMandateHandler goCardlessMandateHandler,
                             MandateStateUpdater mandateStateUpdater,
                             PaymentStateUpdater paymentStateUpdater,
                             MandateQueryService mandateQueryService,
                             PaymentQueryService paymentQueryService) {
        this.goCardlessService = goCardlessService;
        this.goCardlessPaymentHandler = goCardlessPaymentHandler;
        this.goCardlessMandateHandler = goCardlessMandateHandler;
        this.mandateStateUpdater = mandateStateUpdater;
        this.paymentStateUpdater = paymentStateUpdater;
        this.mandateQueryService = mandateQueryService;
        this.paymentQueryService = paymentQueryService;
    }

    public void handleEvents(List<GoCardlessEvent> events) {
        events.forEach(goCardlessService::storeEvent);
        updateStatesForEvents(events);
        var mapOfBooleanToListOfEvents = events.stream().collect(partitioningBy(event -> shouldBeHandled(event)));
        handleValidEvents(mapOfBooleanToListOfEvents.get(true));
        handleInvalidEvents(mapOfBooleanToListOfEvents.get(false));
    }

    private void updateStatesForEvents(List<GoCardlessEvent> events) {
        Map<GoCardlessResourceType, List<GoCardlessEvent>> eventsGroupedByResourceType = events.stream().collect(groupingBy(GoCardlessEvent::getResourceType));
        updateStateForMandateEvents(eventsGroupedByResourceType.getOrDefault(MANDATES, Collections.emptyList()));
        updateStateForPaymentEvents(eventsGroupedByResourceType.getOrDefault(PAYMENTS, Collections.emptyList()));
    }

    private void handleInvalidEvents(List<GoCardlessEvent> events) {
//        LOGGER.info("unhandled resource type for event with id {} ", event.getGoCardlessEventId());
    }

    private void handleValidEvents(List<GoCardlessEvent> events) {
        events.forEach(event -> {
            LOGGER.info("About to handle event of type: {}, action: {}, resource id: {}",
                    event.getResourceType(),
                    event.getAction(),
                    event.getResourceId());
            
            if (event.getResourceType() == MANDATES) {
                goCardlessMandateHandler.handle(event);
            } else {
                goCardlessPaymentHandler.handle(event);
            }
        });
    }

    private static boolean shouldBeHandled(GoCardlessEvent event) {
        if (VALID_RESOURCE_TYPES.contains(event.getResourceType())) {
            if (event.getResourceType().equals(MANDATES)) {
                return GoCardlessMandateHandler.GoCardlessMandateAction.fromString(event.getAction()).isPresent();
            } else {
                return GoCardlessPaymentHandler.GoCardlessPaymentAction.fromString(event.getAction()).isPresent();
            }
        }
        return false;
    }

    private void updateStateForMandateEvents(List<GoCardlessEvent> eventsThatAffectMandates) {
        eventsThatAffectMandates.stream()
                .map(this::toGoCardlessMandateIdAndOrganisationId)
                .flatMap(Optional::stream)
                .distinct()
                .map(this::getMandate)
                .flatMap(Optional::stream)
                .forEach(mandateStateUpdater::updateStateIfNecessary);
    }

    private Optional<Pair<GoCardlessMandateId, GoCardlessOrganisationId>> toGoCardlessMandateIdAndOrganisationId(GoCardlessEvent goCardlessEvent) {
        var goCardlessMandateIdAndOrganisationId = goCardlessEvent.getLinksMandate()
                .map(mandateId -> Pair.of(mandateId, goCardlessEvent.getLinksOrganisation()))
                .orElseGet(() -> {
                    LOGGER.error(format("GoCardless event %s has resource_type mandate but no links.mandate", goCardlessEvent.getGoCardlessEventId()));
                    return null;
                });
        return Optional.ofNullable(goCardlessMandateIdAndOrganisationId);
    }

    private Optional<Mandate> getMandate(Pair<GoCardlessMandateId, GoCardlessOrganisationId> goCardlessMandateIdAndOrganisationId) {
        GoCardlessMandateId goCardlessMandateId = goCardlessMandateIdAndOrganisationId.getLeft();
        GoCardlessOrganisationId goCardlessOrganisationId = goCardlessMandateIdAndOrganisationId.getRight();
        try {
            Mandate mandate = mandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                    goCardlessMandateId,
                    goCardlessOrganisationId);

            return Optional.of(mandate);
        } catch (MandateNotFoundException e) {
            LOGGER.error(format("Could not update status of GoCardless mandate %s for organisation %s because the mandate was not found",
                    goCardlessMandateId,
                    goCardlessOrganisationId));
        }

        return Optional.empty();
    }

    private void updateStateForPaymentEvents(List<GoCardlessEvent> eventsThatAffectPayments) {
        eventsThatAffectPayments.stream()
                .map(this::toGoCardlessPaymentIdAndOrganisationId)
                .flatMap(Optional::stream)
                .distinct()
                .map(this::getPayment)
                .flatMap(Optional::stream)
                .forEach(paymentStateUpdater::updateStateIfNecessary);
    }

    private Optional<Pair<GoCardlessPaymentId, GoCardlessOrganisationId>> toGoCardlessPaymentIdAndOrganisationId(GoCardlessEvent goCardlessEvent) {
        var goCardlessPaymentIdAndOrganisationId = goCardlessEvent.getLinksPayment()
                .map(paymentId -> Pair.of(paymentId, goCardlessEvent.getLinksOrganisation()))
                .orElseGet(() -> {
                    LOGGER.error(format("GoCardless event %s has reource_type payment but no links.payment", goCardlessEvent.getGoCardlessEventId()));
                    return null;
                });
        return Optional.ofNullable(goCardlessPaymentIdAndOrganisationId);
    }

    private Optional<Payment> getPayment(Pair<GoCardlessPaymentId, GoCardlessOrganisationId> goCardlessPaymentIdAndOrganisationId) {
        GoCardlessPaymentId goCardlessPaymentId = goCardlessPaymentIdAndOrganisationId.getLeft();
        GoCardlessOrganisationId goCardlessOrganisationId = goCardlessPaymentIdAndOrganisationId.getRight();
        return paymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessPaymentId, goCardlessOrganisationId)
                .or(() -> {
                    LOGGER.error(format("Could not update status of GoCardless payment %s for organisation %s because the payment was not found",
                            goCardlessPaymentId,
                            goCardlessOrganisationId));
                    return Optional.empty();
                });
    }
}
