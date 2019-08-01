package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

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
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.PAYMENTS;

public class SendEmailsForGoCardlessEventsHandler {

    private static Predicate<GoCardlessEvent> byValidEventsForMandateResourceTypes =
            event -> MANDATES.equals(event.getResourceType());
    
    private static Predicate<GoCardlessEvent> byValidEventsForPaymentsResourceTypes =
            event -> PAYMENTS.equals(event.getResourceType());

    private final MandateQueryService mandateQueryService;
    private final UserNotificationService userNotificationService;
    private final PaymentQueryService paymentQueryService;

    @Inject
    public SendEmailsForGoCardlessEventsHandler(MandateQueryService mandateQueryService,
                                                UserNotificationService userNotificationService,
                                                PaymentQueryService paymentQueryService) {
        this.mandateQueryService = mandateQueryService;
        this.userNotificationService = userNotificationService;
        this.paymentQueryService = paymentQueryService;
    }

    public void sendEmails(List<GoCardlessEvent> events) {
        events.stream().filter(byValidEventsForMandateResourceTypes).forEach(e -> sendEmailForMandateEvents(e));
        events.stream().filter(byValidEventsForPaymentsResourceTypes).forEach(e -> sendEmailForPaymentEvents(e));
    }

    private void sendEmailForPaymentEvents(GoCardlessEvent event) {
        GoCardlessPaymentId goCardlessPaymentId = event.getLinksPayment()
                .orElseThrow(() -> new GoCardlessEventHasNoPaymentIdException(event.getGoCardlessEventId()));
        
        if (event.getAction().equals("failed")) {
            Payment payment = paymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessPaymentId, event.getLinksOrganisation())
                .orElseThrow(() -> new PaymentNotFoundException(goCardlessPaymentId, event.getLinksOrganisation()));
            userNotificationService.sendPaymentFailedEmailFor(payment);
        }
    }

    private void sendEmailForMandateEvents(GoCardlessEvent event) {
        switch (event.getAction()) {
            case "cancelled": 
                userNotificationService.sendMandateCancelledEmailFor(getMandate(event)); 
                break;
            case "failed": 
                userNotificationService.sendMandateFailedEmailFor(getMandate(event));
        }
    }

    private Mandate getMandate(GoCardlessEvent event) {
        return mandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                    event.getLinksMandate().orElseThrow(() -> new GoCardlessEventHasNoMandateIdException(event.getGoCardlessEventId())),
                    event.getLinksOrganisation());
    }
}
