package uk.gov.pay.directdebit.mandate.services.gocardless;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.mandate.model.MandateState.ACTIVE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CANCELLED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.FAILED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED_TO_PROVIDER;

class GoCardlessEventToMandateStateMapper {
    private static final Map<String, MandateState> GOCARDLESS_ACTION_TO_MANDATE_STATE = Map.of(
            "created", CREATED,
            "submitted", SUBMITTED_TO_PROVIDER,
            "active", ACTIVE,
            "cancelled", CANCELLED,
            "failed", FAILED
    );

    static final Set<String> GOCARDLESS_ACTIONS_THAT_CHANGE_STATE = GOCARDLESS_ACTION_TO_MANDATE_STATE.keySet();
    
    static Optional<DirectDebitStateWithDetails<MandateState>> mapGoCardlessEventToState(GoCardlessEvent goCardlessEvent) {
        return Optional.ofNullable(GOCARDLESS_ACTION_TO_MANDATE_STATE.get(goCardlessEvent.getAction()))
                .map(mandateState -> new DirectDebitStateWithDetails<>(
                        mandateState, goCardlessEvent.getDetailsCause(), goCardlessEvent.getDetailsDescription()));
    }
}
