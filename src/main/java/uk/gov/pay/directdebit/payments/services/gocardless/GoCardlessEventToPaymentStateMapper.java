package uk.gov.pay.directdebit.payments.services.gocardless;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_CANCELLED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_CHARGEBACK_CANCELLED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_CHARGEBACK_SETTLED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_CHARGED_BACK;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_CONFIRMED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_CUSTOMER_APPROVAL_DENIED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_FAILED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_LATE_FAILURE_SETTLED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_PAID_OUT;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.COLLECTED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CUSTOMER_APPROVAL_DENIED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.INDEMNITY_CLAIM;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUBMITTED_TO_BANK;

public class GoCardlessEventToPaymentStateMapper {
    private static final Map<String, PaymentState> GOCARDLESS_ACTION_TO_PAYMENT_STATE = Map.of(
            ACTION_PAYMENT_SUBMITTED, SUBMITTED_TO_BANK,
            ACTION_PAYMENT_FAILED, FAILED,
            ACTION_PAYMENT_PAID_OUT, PAID_OUT,
            ACTION_PAYMENT_CUSTOMER_APPROVAL_DENIED, CUSTOMER_APPROVAL_DENIED,
            ACTION_PAYMENT_CONFIRMED, COLLECTED_BY_PROVIDER,
            ACTION_PAYMENT_CANCELLED, CANCELLED,
            ACTION_PAYMENT_CHARGED_BACK, INDEMNITY_CLAIM,
            ACTION_PAYMENT_CHARGEBACK_CANCELLED, PAID_OUT,
            ACTION_PAYMENT_LATE_FAILURE_SETTLED, FAILED,
            ACTION_PAYMENT_CHARGEBACK_SETTLED, INDEMNITY_CLAIM
    );

    static final Set<String> GOCARDLESS_ACTIONS_THAT_CHANGE_PAYMENT_STATE = GOCARDLESS_ACTION_TO_PAYMENT_STATE.keySet();

    static Optional<DirectDebitStateWithDetails<PaymentState>> mapGoCardlessEventToPaymentState(GoCardlessEvent goCardlessEvent) {
        return Optional.ofNullable(GOCARDLESS_ACTION_TO_PAYMENT_STATE.get(goCardlessEvent.getAction()))
                .map(paymentState -> new DirectDebitStateWithDetails<>(
                        paymentState, goCardlessEvent.getDetailsCause(), goCardlessEvent.getDetailsDescription()));
    }
}
