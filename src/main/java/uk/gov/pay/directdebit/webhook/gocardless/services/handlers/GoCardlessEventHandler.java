package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoMandateIdException;
import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoPaymentIdException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.exception.PaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.PAYOUTS;

public class GoCardlessEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessEventHandler.class);
    
    private static Predicate<GoCardlessEvent> byValidEventsForMandateResourceTypes =
            event -> MANDATES.equals(event.getResourceType()) &&
                    Set.of("created", "submitted", "active", "failed", "cancelled")
                            .contains(event.getAction().toLowerCase());
    
    private static Predicate<GoCardlessEvent> byValidEventsForPaymentsAndPayoutsResourceTypes =
            event -> Set.of(PAYMENTS, PAYOUTS).contains(event.getResourceType()) &&
                    Set.of("created", "submitted", "confirmed", "failed", "paid_out", "paid")
                            .contains(event.getAction().toLowerCase());

    private final MandateQueryService mandateQueryService;
    private final UserNotificationService userNotificationService;
    private final PaymentQueryService paymentQueryService;

    @Inject
    public GoCardlessEventHandler(MandateQueryService mandateQueryService,
                                  UserNotificationService userNotificationService, 
                                  PaymentQueryService paymentQueryService) {
        this.mandateQueryService = mandateQueryService;
        this.userNotificationService = userNotificationService;
        this.paymentQueryService = paymentQueryService;
    }

    public void handle(List<GoCardlessEvent> events) {
        events.stream().filter(byValidEventsForMandateResourceTypes).forEach(e -> handleMandate(e));
        events.stream().filter(byValidEventsForPaymentsAndPayoutsResourceTypes).forEach(e -> handlePayment(e));
    }

    private void handlePayment(GoCardlessEvent event) {
        logEvent(event);
        
        GoCardlessPaymentId goCardlessPaymentId = event.getLinksPayment()
                .orElseThrow(() -> new GoCardlessEventHasNoPaymentIdException(event.getGoCardlessEventId()));

        Payment payment = paymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessPaymentId, event.getLinksOrganisation())
                .orElseThrow(() -> new PaymentNotFoundException(goCardlessPaymentId, event.getLinksOrganisation()));

        if (event.getAction().equalsIgnoreCase("failed")) {
            userNotificationService.sendPaymentFailedEmailFor(payment);
        }
    }

    private void handleMandate(GoCardlessEvent event) {
        logEvent(event);
        
        Mandate mandate = mandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                event.getLinksMandate().orElseThrow(() -> new GoCardlessEventHasNoMandateIdException(event.getGoCardlessEventId())),
                event.getLinksOrganisation());
        
        switch (event.getAction().toLowerCase()) {
            case "cancelled": userNotificationService.sendMandateCancelledEmailFor(mandate); break;
            case "failed": userNotificationService.sendMandateFailedEmailFor(mandate);
        }
    }

    private static void logEvent(GoCardlessEvent event) {
        LOGGER.info("About to handle event of type: {}, action: {}, resource id: {}",
                event.getResourceType(),
                event.getAction(),
                event.getResourceId());
    }
}
