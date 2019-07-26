package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_USER_SETUP_CANCELLED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_USER_SETUP_CANCELLED_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_USER_SETUP_EXPIRED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.mandate.model.MandateState.USER_CANCEL_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.USER_SETUP_CANCELLED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.USER_SETUP_EXPIRED;

public class GovUkPayEventToMandateStateMapper {
    private static final Map<GovUkPayEventType, MandateState> GOV_UK_PAY_EVENT_TYPE_TO_MANDATE_STATE = Map.of(
            MANDATE_CREATED, CREATED,
            MANDATE_TOKEN_EXCHANGED, AWAITING_DIRECT_DEBIT_DETAILS,
            MANDATE_SUBMITTED_TO_PROVIDER, SUBMITTED_TO_PROVIDER,
            MANDATE_USER_SETUP_EXPIRED, USER_SETUP_EXPIRED,
            MANDATE_USER_SETUP_CANCELLED, USER_SETUP_CANCELLED,
            MANDATE_USER_SETUP_CANCELLED_NOT_ELIGIBLE, USER_CANCEL_NOT_ELIGIBLE
    );

    public static final Set<GovUkPayEventType> GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_MANDATE_STATE = GOV_UK_PAY_EVENT_TYPE_TO_MANDATE_STATE.keySet();

    public static Optional<DirectDebitStateWithDetails<MandateState>> mapGovUkPayEventToMandateState(GovUkPayEvent govUkPayEvent) {
        return Optional.ofNullable(GOV_UK_PAY_EVENT_TYPE_TO_MANDATE_STATE.get(govUkPayEvent.getEventType()))
                .map(DirectDebitStateWithDetails::new);
    }
}
