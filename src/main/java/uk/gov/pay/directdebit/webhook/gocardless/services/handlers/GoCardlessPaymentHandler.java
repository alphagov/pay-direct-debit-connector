package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoPaymentIdException;
import uk.gov.pay.directdebit.payments.exception.InvalidStateException;
import uk.gov.pay.directdebit.payments.exception.PaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.GoCardlessEventService;
import uk.gov.pay.directdebit.payments.services.PaymentService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;

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

        public static Optional<GoCardlessPaymentAction> fromString(String type) {
            for (GoCardlessPaymentAction typeEnum : values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    return Optional.of(typeEnum);
                }
            }
            LOGGER.error("Webhook from GoCardless with unrecognised payment action: {}", type);
            return Optional.empty();
        }
    }

    @Override
    protected Optional<DirectDebitEvent> process(GoCardlessEvent event) {
        var goCardlessPaymentId = event.getLinksPayment()
                .orElseThrow(() -> new GoCardlessEventHasNoPaymentIdException(event.getGoCardlessEventId()));

        var goCardlessOrganisationId = event.getLinksOrganisation();

        Payment payment = paymentService.findPaymentByProviderId(GOCARDLESS, goCardlessPaymentId, goCardlessOrganisationId)
                .orElseThrow(() -> new PaymentNotFoundException(GOCARDLESS, goCardlessPaymentId, goCardlessOrganisationId));

        return GoCardlessPaymentAction.fromString(event.getAction())
                .map(action -> getHandledActions().get(action))
                .map(handledAction -> handledAction.apply(payment));
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
                .map(organisationIdentifier -> organisationIdentifier.equals(event.getLinksOrganisation()))
                .orElse(false);
    }
}
