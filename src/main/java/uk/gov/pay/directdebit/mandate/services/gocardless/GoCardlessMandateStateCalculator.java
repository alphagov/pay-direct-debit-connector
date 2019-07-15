package uk.gov.pay.directdebit.mandate.services.gocardless;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payments.model.GoCardlessMandateIdAndOrganisationId;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.mandate.model.MandateState.ACTIVE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CANCELLED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.FAILED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED;

class GoCardlessMandateStateCalculator {

    private final GoCardlessEventDao goCardlessEventDao;

    private static final Map<String, MandateState> GOCARDLESS_ACTION_TO_MANDATE_STATE = Map.of(
            "created", CREATED,
            "submitted", SUBMITTED,
            "active", ACTIVE,
            "cancelled", CANCELLED,
            "failed", FAILED
    );

    static final Set<String> GOCARDLESS_ACTIONS_THAT_CHANGE_STATE = GOCARDLESS_ACTION_TO_MANDATE_STATE.keySet();

    @Inject
    GoCardlessMandateStateCalculator(GoCardlessEventDao goCardlessEventDao) {
        this.goCardlessEventDao = goCardlessEventDao;
    }

    Optional<DirectDebitStateWithDetails<MandateState>> calculate(GoCardlessMandateIdAndOrganisationId goCardlessMandateIdAndOrganisationId) {
        return goCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateIdAndOrganisationId, GOCARDLESS_ACTIONS_THAT_CHANGE_STATE)
                .filter(goCardlessEvent -> GOCARDLESS_ACTION_TO_MANDATE_STATE.get(goCardlessEvent.getAction()) != null)
                .map(goCardlessEvent -> new DirectDebitStateWithDetails<>(
                        GOCARDLESS_ACTION_TO_MANDATE_STATE.get(goCardlessEvent.getAction()),
                        goCardlessEvent.getDetailsCause(),
                        goCardlessEvent.getDetailsDescription())
                );
    }

}
