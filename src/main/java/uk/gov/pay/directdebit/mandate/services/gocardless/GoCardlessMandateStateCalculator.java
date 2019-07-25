package uk.gov.pay.directdebit.mandate.services.gocardless;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.model.Event;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountMissingOrganisationIdException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.services.MandateStateCalculator;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.mandate.services.GovUkPayEventToMandateStateMapper.GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_MANDATE_STATE;
import static uk.gov.pay.directdebit.mandate.services.GovUkPayEventToMandateStateMapper.mapGovUkPayEventToMandateState;
import static uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessEventToMandateStateMapper.GOCARDLESS_ACTIONS_THAT_CHANGE_MANDATE_STATE;
import static uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessEventToMandateStateMapper.mapGoCardlessEventToMandateState;

public class GoCardlessMandateStateCalculator implements MandateStateCalculator {

    private final GoCardlessEventDao goCardlessEventDao;
    private final GovUkPayEventDao govUkPayEventDao;


    @Inject
    GoCardlessMandateStateCalculator(GoCardlessEventDao goCardlessEventDao, GovUkPayEventDao govUkPayEventDao) {
        this.goCardlessEventDao = goCardlessEventDao;
        this.govUkPayEventDao = govUkPayEventDao;
    }

    public Optional<DirectDebitStateWithDetails<MandateState>> calculate(Mandate mandate) {
        Optional<GoCardlessEvent> latestApplicableGoCardlessEvent = getLatestApplicableGoCardlessEvent(mandate);

        Optional<GovUkPayEvent> latestApplicableGovUkPayEvent
                = govUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_MANDATE_STATE);

        return Stream.of(latestApplicableGoCardlessEvent, latestApplicableGovUkPayEvent)
                .flatMap(Optional::stream)
                .max(Comparator.comparing(Event::getTimestamp))
                .flatMap(this::mapEventToState);
    }

    private Optional<GoCardlessEvent> getLatestApplicableGoCardlessEvent(Mandate mandate) {
        return mandate.getPaymentProviderMandateId()
                .flatMap(paymentProviderMandateId -> {
                    GoCardlessOrganisationId goCardlessOrganisationId = mandate.getGatewayAccount().getOrganisation()
                            .orElseThrow(() -> new GatewayAccountMissingOrganisationIdException(mandate.getGatewayAccount()));

                    return goCardlessEventDao.findLatestApplicableEventForMandate(
                            (GoCardlessMandateId) paymentProviderMandateId,
                            goCardlessOrganisationId,
                            GOCARDLESS_ACTIONS_THAT_CHANGE_MANDATE_STATE);
                });
    }

    private Optional<DirectDebitStateWithDetails<MandateState>> mapEventToState(Event event) {
        if (event instanceof GoCardlessEvent) {
            return mapGoCardlessEventToMandateState((GoCardlessEvent) event);
        } else if (event instanceof GovUkPayEvent) {
            return mapGovUkPayEventToMandateState((GovUkPayEvent) event);
        } else {
            throw new IllegalArgumentException(format("Unexpected Event of type %s", event.getClass()));
        }
    }

}
