package uk.gov.pay.directdebit.mandate.services.gocardless;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.model.Event;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountMissingOrganisationIdException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_EXPIRED_BY_SYSTEM;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_SUBMITTED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.ACTIVE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CANCELLED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.EXPIRED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.FAILED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.USER_CANCEL_NOT_ELIGIBLE;

class GoCardlessMandateStateCalculator {

    private final GoCardlessEventDao goCardlessEventDao;
    private final GovUkPayEventDao govUkPayEventDao;

    private static final Map<String, MandateState> GOCARDLESS_ACTION_TO_MANDATE_STATE = Map.of(
            "created", CREATED,
            "submitted", SUBMITTED,
            "active", ACTIVE,
            "cancelled", CANCELLED,
            "failed", FAILED
    );

    private static final Map<GovUkPayEventType, MandateState> GOV_UK_PAY_EVENT_TYPE_TO_MANDATE_STATE = Map.of(
            MANDATE_CREATED, CREATED,
            MANDATE_TOKEN_EXCHANGED, AWAITING_DIRECT_DEBIT_DETAILS,
            MANDATE_SUBMITTED, SUBMITTED,
            MANDATE_EXPIRED_BY_SYSTEM, EXPIRED,
            MANDATE_CANCELLED_BY_USER, CANCELLED,
            MANDATE_CANCELLED_BY_USER_NOT_ELIGIBLE, USER_CANCEL_NOT_ELIGIBLE
    );

    static final Set<String> GOCARDLESS_ACTIONS_THAT_CHANGE_STATE = GOCARDLESS_ACTION_TO_MANDATE_STATE.keySet();

    static final Set<GovUkPayEventType> GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE = GOV_UK_PAY_EVENT_TYPE_TO_MANDATE_STATE.keySet();

    @Inject
    GoCardlessMandateStateCalculator(GoCardlessEventDao goCardlessEventDao, GovUkPayEventDao govUkPayEventDao) {
        this.goCardlessEventDao = goCardlessEventDao;
        this.govUkPayEventDao = govUkPayEventDao;
    }

    Optional<DirectDebitStateWithDetails<MandateState>> calculate(Mandate mandate) {
        Optional<GoCardlessEvent> latestApplicableGoCardlessEvent = getLatestApplicableGoCardlessEvent(mandate);

        Optional<GovUkPayEvent> latestApplicableGovUkPayEvent
                = govUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE);

        return Stream.of(latestApplicableGoCardlessEvent, latestApplicableGovUkPayEvent)
                .flatMap(Optional::stream)
                .max(Comparator.comparing(Event::getTimestamp))
                .map(this::mapEventToState);
    }

    private Optional<GoCardlessEvent> getLatestApplicableGoCardlessEvent(Mandate mandate) {
        return mandate.getPaymentProviderMandateId()
                .map(paymentProviderMandateId -> {
                    GoCardlessOrganisationId goCardlessOrganisationId = mandate.getGatewayAccount().getOrganisation()
                            .orElseThrow(() -> new GatewayAccountMissingOrganisationIdException(mandate.getGatewayAccount()));

                    return goCardlessEventDao.findLatestApplicableEventForMandate(
                            (GoCardlessMandateId) paymentProviderMandateId,
                            goCardlessOrganisationId,
                            GOCARDLESS_ACTIONS_THAT_CHANGE_STATE);
                })
                .orElse(Optional.empty());
    }

    private DirectDebitStateWithDetails<MandateState> mapEventToState(Event event) {
        if (event instanceof GoCardlessEvent) {
            return mapGoCardlessEventToState((GoCardlessEvent) event);
        } else if (event instanceof GovUkPayEvent) {
            return mapGovUkPayEventToState((GovUkPayEvent) event);
        } else {
            throw new IllegalArgumentException(format("Unexpected Event of type %s", event.getClass()));
        }
    }

    private DirectDebitStateWithDetails<MandateState> mapGoCardlessEventToState(GoCardlessEvent goCardlessEvent) {
        return new DirectDebitStateWithDetails<>(
                GOCARDLESS_ACTION_TO_MANDATE_STATE.get(goCardlessEvent.getAction()),
                goCardlessEvent.getDetailsCause(),
                goCardlessEvent.getDetailsDescription());
    }

    private DirectDebitStateWithDetails<MandateState> mapGovUkPayEventToState(GovUkPayEvent govUkPayEvent) {
        return new DirectDebitStateWithDetails<>(GOV_UK_PAY_EVENT_TYPE_TO_MANDATE_STATE.get(govUkPayEvent.getEventType()));
    }

}
