package uk.gov.pay.directdebit.payments.services.sandbox;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.SandboxEventDao;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.payments.services.PaymentStateCalculator;
import uk.gov.pay.directdebit.webhook.sandbox.resources.WebhookSandboxResource;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.payments.model.PaymentState.SUCCESS;

public class SandboxPaymentStateCalculator implements PaymentStateCalculator {

    private final SandboxEventDao sandboxEventDao;

    private static final Map<String, PaymentState> SANDBOX_ACTION_TO_PAYMENT_STATE = Map.of(
            WebhookSandboxResource.SandboxEventAction.PAID_OUT.toString(), SUCCESS
    );

    static final Set<String> SANDBOX_ACTIONS_THAT_CHANGE_STATE = SANDBOX_ACTION_TO_PAYMENT_STATE.keySet();

    @Inject
    SandboxPaymentStateCalculator(SandboxEventDao sandboxEventDao) {
        this.sandboxEventDao = sandboxEventDao;
    }

    public Optional<DirectDebitStateWithDetails<PaymentState>> calculate(Payment payment) {
        return getLatestApplicableSandboxEvent(payment)
                .filter(sandboxEvent -> SANDBOX_ACTION_TO_PAYMENT_STATE.get(sandboxEvent.getEventAction()) != null)
                .map(sandboxEvent -> new DirectDebitStateWithDetails<>(
                        SANDBOX_ACTION_TO_PAYMENT_STATE.get(sandboxEvent.getEventAction())
                ));
    }

    private Optional<SandboxEvent> getLatestApplicableSandboxEvent(Payment payment) {
        return payment.getProviderId().flatMap(providerId ->
                sandboxEventDao.findLatestApplicableEventForPayment(
                        (SandboxPaymentId) providerId,
                        SANDBOX_ACTIONS_THAT_CHANGE_STATE));
    }

}
