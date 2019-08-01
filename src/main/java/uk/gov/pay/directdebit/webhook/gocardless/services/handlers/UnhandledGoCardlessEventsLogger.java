package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessResourceType;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_CUSTOMER_APPROVAL_GRANTED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_CUSTOMER_APPROVAL_SKIPPED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_REPLACED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_RESUBMISSION_REQUESTED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_RESUBMISSION_REQUESTED;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.PAYMENTS;

public class UnhandledGoCardlessEventsLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnhandledGoCardlessEventsLogger.class);

    private static Set<String> UNHANDLED_MANDATE_ACTIONS = Set.of(
            ACTION_MANDATE_REPLACED,
            ACTION_MANDATE_CUSTOMER_APPROVAL_GRANTED,
            ACTION_MANDATE_CUSTOMER_APPROVAL_SKIPPED,
            ACTION_MANDATE_RESUBMISSION_REQUESTED);

    private static Set<String> UNHANDLED_PAYMENT_ACTIONS = Set.of(
            ACTION_PAYMENT_RESUBMISSION_REQUESTED
    );

    private static Predicate<GoCardlessEvent> byUnhandledMandateActions =
            event -> MANDATES.equals(event.getResourceType())
                    && UNHANDLED_MANDATE_ACTIONS.contains(event.getAction());

    private static Predicate<GoCardlessEvent> byUndandledPaymentActions =
            event -> PAYMENTS.equals(event.getResourceType())
                    && UNHANDLED_PAYMENT_ACTIONS.contains(event.getAction());

    private static Predicate<GoCardlessEvent> byUnhandledResourceTypes =
            event -> Set.of(GoCardlessResourceType.REFUNDS, GoCardlessResourceType.SUBSCRIPTIONS)
                    .contains(event.getResourceType());

    public void logUnhandledEvents(List<GoCardlessEvent> events) {
        events.stream().filter(byUnhandledMandateActions).forEach(this::logErrorForUnexpectedMandateEvent);
        events.stream().filter(byUndandledPaymentActions).forEach(this::logErrorForUnexpectedPaymentEvent);
        events.stream().filter(byUnhandledResourceTypes).forEach(event ->
                LOGGER.error("Received a GoCardless event with id {} for organisation {} with resource type {}, which " +
                                "we do not expect to receive and do not handle.", event.getGoCardlessEventId(),
                        event.getLinksOrganisation(), event.getResourceType()));
    }

    private void logErrorForUnexpectedMandateEvent(GoCardlessEvent event) {
        event.getLinksMandate().ifPresentOrElse(
                goCardlessMandateId -> LOGGER.error("Received a GoCardless event with id {} for organisation {} with " +
                                "action {} for mandate {}, which we do not expect to receive and do not handle.",
                        event.getGoCardlessEventId(), event.getLinksOrganisation(), event.getAction(), goCardlessMandateId),
                () -> LOGGER.error("Received a GoCardless event with id {} for organisation {} with action {} for an " +
                                "unspecified mandate, which we do not expect to receive and do not handle.",
                        event.getGoCardlessEventId(), event.getLinksOrganisation(), event.getAction()));
    }

    private void logErrorForUnexpectedPaymentEvent(GoCardlessEvent event) {
        event.getLinksPayment().ifPresentOrElse(
                goCardlessPaymentId -> LOGGER.error("Received a GoCardless event with id {} for organisation {} with " +
                                "action {} for payment {}, which we do not expect to receive and do not handle.",
                        event.getGoCardlessEventId(), event.getLinksOrganisation(), event.getAction(), goCardlessPaymentId),
                () -> LOGGER.error("Received a GoCardless event with id {} for organisation {} with action {} for an " +
                                "unspecified payment, which we do not expect to receive and do not handle.",
                        event.getGoCardlessEventId(), event.getLinksOrganisation(), event.getAction()));
    }
}
