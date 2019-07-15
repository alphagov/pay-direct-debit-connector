package uk.gov.pay.directdebit.payments.services.sandbox;

import uk.gov.pay.directdebit.events.dao.SandboxEventDao;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.webhook.sandbox.resources.WebhookSandboxResource;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.payments.model.PaymentState.SUCCESS;

public class SandboxPaymentStateCalculator {

    private final SandboxEventDao sandboxEventDao;

    private static final Map<String, PaymentState> SANDBOX_ACTION_TO_PAYMENT_STATE = Map.of(
            WebhookSandboxResource.SandboxEventAction.PAID_OUT.toString(), SUCCESS
    );

    static final Set<String> SANDBOX_ACTIONS_THAT_CHANGE_STATE = SANDBOX_ACTION_TO_PAYMENT_STATE.keySet();

    @Inject
    SandboxPaymentStateCalculator(SandboxEventDao sandboxEventDao) {
        this.sandboxEventDao = sandboxEventDao;
    }

    Optional<PaymentState> calculate(SandboxPaymentId sandboxPaymentId) {
        return sandboxEventDao.findLatestApplicableEventForPayment(sandboxPaymentId, SANDBOX_ACTIONS_THAT_CHANGE_STATE)
                .map(SandboxEvent::getEventAction)
                .map(SANDBOX_ACTION_TO_PAYMENT_STATE::get);
    }

}
