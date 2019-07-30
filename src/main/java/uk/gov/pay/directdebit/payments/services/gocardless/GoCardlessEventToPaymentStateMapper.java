package uk.gov.pay.directdebit.payments.services.gocardless;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.payments.model.PaymentState.CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.COLLECTED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CUSTOMER_APPROVAL_DENIED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.INDEMNITY_CLAIM;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUBMITTED_TO_BANK;

public class GoCardlessEventToPaymentStateMapper {
    private static final Map<String, PaymentState> GOCARDLESS_ACTION_TO_PAYMENT_STATE = Map.of(
            "submitted", SUBMITTED_TO_BANK,
            "failed", FAILED,
            "paid_out", PAID_OUT,
            "customer_approval_denied", CUSTOMER_APPROVAL_DENIED,
            "confirmed", COLLECTED_BY_PROVIDER,
            "cancelled", CANCELLED,
            "charged_back", INDEMNITY_CLAIM
    );

    static final Set<String> GOCARDLESS_ACTIONS_THAT_CHANGE_PAYMENT_STATE = GOCARDLESS_ACTION_TO_PAYMENT_STATE.keySet();

    static Optional<DirectDebitStateWithDetails<PaymentState>> mapGoCardlessEventToPaymentState(GoCardlessEvent goCardlessEvent) {
        return Optional.ofNullable(GOCARDLESS_ACTION_TO_PAYMENT_STATE.get(goCardlessEvent.getAction()))
                .map(paymentState -> new DirectDebitStateWithDetails<>(
                        paymentState, goCardlessEvent.getDetailsCause(), goCardlessEvent.getDetailsDescription()));
    }
}
