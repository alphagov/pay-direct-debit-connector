package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoPaymentIdException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessPaymentAction;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.exception.PaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Consumer;

public class GoCardlessPaymentHandler {

    private final PaymentQueryService paymentQueryService;
    private final UserNotificationService userNotificationService;

    @Inject
    public GoCardlessPaymentHandler(PaymentQueryService paymentQueryService,
                                    UserNotificationService userNotificationService) {
        this.paymentQueryService = paymentQueryService;
        this.userNotificationService = userNotificationService;
    }

    public void handle(GoCardlessEvent event, GoCardlessPaymentAction action) {
        GoCardlessPaymentId goCardlessPaymentId = event.getLinksPayment()
                .orElseThrow(() -> new GoCardlessEventHasNoPaymentIdException(event.getGoCardlessEventId()));

        Payment payment = paymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessPaymentId, event.getLinksOrganisation())
                .orElseThrow(() -> new PaymentNotFoundException(goCardlessPaymentId, event.getLinksOrganisation()));

        getHandledActions().getOrDefault(action, p -> {}).accept(payment);
    }

    private Map<GoCardlessPaymentAction, Consumer<Payment>> getHandledActions() {
        return Map.of(GoCardlessPaymentAction.FAILED, userNotificationService::sendPaymentFailedEmailFor);
    }

}
