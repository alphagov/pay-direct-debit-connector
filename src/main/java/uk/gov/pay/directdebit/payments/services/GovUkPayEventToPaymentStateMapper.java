package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_ERROR_SUBMITTING_TO_PROVIDER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_SUBMITTED;

public class GovUkPayEventToPaymentStateMapper {
    private static final Map<GovUkPayEventType, PaymentState> GOV_UK_PAY_EVENT_TYPE_TO_PAYMENT_STATE = Map.of(
            PAYMENT_CREATED, PaymentState.CREATED,
            PAYMENT_SUBMITTED, PaymentState.SUBMITTED_TO_PROVIDER,
            PAYMENT_ERROR_SUBMITTING_TO_PROVIDER, PaymentState.ERROR_SUBMITTING_TO_PROVIDER
    );

    public static final Set<GovUkPayEventType> GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_PAYMENT_STATE = GOV_UK_PAY_EVENT_TYPE_TO_PAYMENT_STATE.keySet();

    public static Optional<DirectDebitStateWithDetails<PaymentState>> mapGovUkPayEventToPaymentState(GovUkPayEvent govUkPayEvent) {
        return Optional.ofNullable(GOV_UK_PAY_EVENT_TYPE_TO_PAYMENT_STATE.get(govUkPayEvent.getEventType()))
                .map(DirectDebitStateWithDetails::new);
    }
}
