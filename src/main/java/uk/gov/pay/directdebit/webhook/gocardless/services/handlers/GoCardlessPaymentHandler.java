package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.exception.InvalidStateException;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.GoCardlessEventService;
import uk.gov.pay.directdebit.payments.services.PaymentService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

public class GoCardlessPaymentHandler extends GoCardlessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessPaymentHandler.class);

    @Inject
    public GoCardlessPaymentHandler(PaymentService paymentService,
                                    GoCardlessEventService goCardlessService) {
        super(paymentService, goCardlessService);
    }

    /**
     * GoCardless payment actions
     *
     * @see <a href="https://developer.gocardless.com/api-reference/#events-payment-actions">https://developer.gocardless.com/api-reference/#events-payment-actions</a>
     */
    public enum GoCardlessPaymentAction implements GoCardlessAction {
        CREATED, SUBMITTED, CONFIRMED, FAILED, PAID_OUT, PAID;

        public static GoCardlessPaymentAction fromString(String type) {
            for (GoCardlessPaymentAction typeEnum : values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    return typeEnum;
                }
            }
            LOGGER.error("Webhook from GoCardless with unrecognised payment action: {}", type);
            return null;
        }
    }

    @Override
    protected Optional<DirectDebitEvent> process(GoCardlessEvent event) {
        return Optional.ofNullable(GoCardlessPaymentAction.fromString(event.getAction()))
                .map((action) -> getHandledActions().get(action))
                .map((handledAction) -> {
                    GoCardlessPayment goCardlessPayment = goCardlessService.findPaymentForEvent(event);
                    Payment payment = paymentService.findPayment(goCardlessPayment.getTransactionId());
                    if (isValidOrganisation(payment, event)) {
                        return handledAction.apply(payment);
                    } else {
                        LOGGER.info("Event from GoCardless with goCardlessEventId: {} has unrecognised organisation: {}",
                                event.getGoCardlessEventId(), event.getOrganisationIdentifier());
                        return null;
                    }
                });
    }

    private Map<GoCardlessAction, Function<Payment, DirectDebitEvent>> getHandledActions() {
        return ImmutableMap.<GoCardlessAction, Function<Payment, DirectDebitEvent>>builder()
                .put(GoCardlessPaymentAction.CREATED, paymentService::paymentAcknowledgedFor)
                .put(GoCardlessPaymentAction.SUBMITTED, paymentService::paymentSubmittedFor)
                .put(GoCardlessPaymentAction.CONFIRMED, (Payment payment) ->
                        paymentService.findPaymentSubmittedEventFor(payment)
                                .orElseThrow(() -> new InvalidStateException("Could not find payment submitted event for payment with id: " + payment.getExternalId())))
                .put(GoCardlessPaymentAction.FAILED, (Payment payment) -> paymentService.paymentFailedWithEmailFor(payment))
                .put(GoCardlessPaymentAction.PAID_OUT, paymentService::paymentPaidOutFor)
                .put(GoCardlessPaymentAction.PAID, paymentService::payoutPaidFor)
                .build();
    }

    private boolean isValidOrganisation(Payment payment, GoCardlessEvent event) {
        
        return payment.getMandate().getGatewayAccount().getOrganisation()
                .map(organisationIdentifier -> organisationIdentifier.equals(event.getOrganisationIdentifier()))
                .orElse(false);
    }
}
