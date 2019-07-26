package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoPaymentIdException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.services.GoCardlessEventService;
import uk.gov.pay.directdebit.payments.exception.PaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.payments.services.PaymentService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class GoCardlessPaymentHandler extends GoCardlessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessPaymentHandler.class);

    private final PaymentQueryService paymentQueryService;

    @Inject
    public GoCardlessPaymentHandler(PaymentService paymentService, PaymentQueryService paymentQueryService,
                                    GoCardlessEventService goCardlessService) {
        super(paymentService, goCardlessService);
        this.paymentQueryService = paymentQueryService;
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
    protected void process(GoCardlessEvent event) {
        GoCardlessPaymentId goCardlessPaymentId = event.getLinksPayment()
                .orElseThrow(() -> new GoCardlessEventHasNoPaymentIdException(event.getGoCardlessEventId()));

        Payment payment = paymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessPaymentId, event.getLinksOrganisation())
                .orElseThrow(() -> new PaymentNotFoundException(goCardlessPaymentId, event.getLinksOrganisation()));

        GoCardlessPaymentAction.fromString(event.getAction())
                .map(action -> getHandledActions().get(action))
                .ifPresent(handledAction -> handledAction.accept(payment));
    }

    private Map<GoCardlessAction, Consumer<Payment>> getHandledActions() {
        return ImmutableMap.of(GoCardlessPaymentAction.FAILED, paymentService::paymentFailedWithEmailFor);
    }

}
