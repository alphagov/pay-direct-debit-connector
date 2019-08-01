package uk.gov.pay.directdebit.mandate.services.gocardless;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_EXPIRED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_FAILED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_REINSTATED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_SUBMITTED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.ACTIVE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CANCELLED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.EXPIRED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.FAILED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED_TO_BANK;

class GoCardlessEventToMandateStateMapper {
    private static final Map<String, MandateState> GOCARDLESS_ACTION_TO_MANDATE_STATE = Map.of(
            ACTION_MANDATE_SUBMITTED, SUBMITTED_TO_BANK,
            ACTION_MANDATE_ACTIVE, ACTIVE,
            ACTION_MANDATE_FAILED, FAILED,
            ACTION_MANDATE_CANCELLED, CANCELLED,
            ACTION_MANDATE_EXPIRED, EXPIRED,
            ACTION_MANDATE_REINSTATED, ACTIVE
    );

    static final Set<String> GOCARDLESS_ACTIONS_THAT_CHANGE_MANDATE_STATE = GOCARDLESS_ACTION_TO_MANDATE_STATE.keySet();

    static Optional<DirectDebitStateWithDetails<MandateState>> mapGoCardlessEventToMandateState(GoCardlessEvent goCardlessEvent) {
        return Optional.ofNullable(GOCARDLESS_ACTION_TO_MANDATE_STATE.get(goCardlessEvent.getAction()))
                .map(mandateState -> new DirectDebitStateWithDetails<>(
                        mandateState, goCardlessEvent.getDetailsCause(), goCardlessEvent.getDetailsDescription()));
    }
}
