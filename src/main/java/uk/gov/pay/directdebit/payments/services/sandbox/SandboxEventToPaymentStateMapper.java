package uk.gov.pay.directdebit.payments.services.sandbox;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.model.GoCardlessPaymentAction;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.payments.model.PaymentState.PAID_OUT;

public class SandboxEventToPaymentStateMapper {
    private static final Map<String, PaymentState> SANDBOX_ACTION_TO_PAYMENT_STATE = Map.of("PAID_OUT", PAID_OUT);

    static final Set<String> SANDBOX_ACTIONS_THAT_CHANGE_PAYMENT_STATE = SANDBOX_ACTION_TO_PAYMENT_STATE.keySet();

    public static Optional<DirectDebitStateWithDetails<PaymentState>> mapSandboxEventToPaymentState(SandboxEvent sandboxEvent) {
        return Optional.ofNullable(SANDBOX_ACTION_TO_PAYMENT_STATE.get(sandboxEvent.getEventAction()))
                .map(DirectDebitStateWithDetails::new);
    }
}
