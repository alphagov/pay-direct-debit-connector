package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;

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

    private static Predicate<GoCardlessEvent> byValidEventsForMandateResourceTypes =
            event -> MANDATES.equals(event.getResourceType());

    private static Predicate<GoCardlessEvent> byValidEventsForPaymentsResourceTypes =
            event -> PAYMENTS.equals(event.getResourceType());
    
    private static Set<String> UNHANDLED_MANDATE_ACTIONS = Set.of(
            ACTION_MANDATE_REPLACED,
            ACTION_MANDATE_CUSTOMER_APPROVAL_GRANTED,
            ACTION_MANDATE_CUSTOMER_APPROVAL_SKIPPED,
            ACTION_MANDATE_RESUBMISSION_REQUESTED);
    
    private static Set<String> UNHANDLED_PAYMENT_ACTIONS = Set.of(
            ACTION_PAYMENT_RESUBMISSION_REQUESTED
    );
    
    public void logUnhandledEvents(List<GoCardlessEvent> events) {
        events.stream().filter(byValidEventsForMandateResourceTypes).forEach(this::logErrorForUnexpectedMandateEvents);
        events.stream().filter(byValidEventsForPaymentsResourceTypes).forEach(this::logErrorForUnexpectedPaymentEvents);
    }
    
    private void logErrorForUnexpectedMandateEvents(GoCardlessEvent event) {
        if (UNHANDLED_MANDATE_ACTIONS.contains(event.getAction())) {
            event.getLinksMandate().ifPresentOrElse(
                    goCardlessMandateId -> LOGGER.error("Received a GoCardless event with action {} for mandate {}, " +
                            "which we do not expect to receive and do not handle", event.getAction(), goCardlessMandateId),
                    () -> LOGGER.error("Received a GoCardless event with action {} for an unspecified mandate, which we " +
                            "do not expect to receive and do not handle", event.getAction()));
        }
    }

    private void logErrorForUnexpectedPaymentEvents(GoCardlessEvent event) {
        if (UNHANDLED_PAYMENT_ACTIONS.contains(event.getAction())) {
            event.getLinksPayment().ifPresentOrElse(
                    goCardlessPaymentId -> LOGGER.error("Received a GoCardless event with action {} for payment {}, which " +
                            "we do not expect to receive and do not handle", event.getAction(), goCardlessPaymentId),
                    () -> LOGGER.error("Received a GoCardless event with action {} for an unspecified payment, which we do " +
                            "not expect to receive and do not handle", event.getAction()));
        }
    }
}
