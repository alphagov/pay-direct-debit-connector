package uk.gov.pay.directdebit.mandate.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.model.Event;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

public class MandateEventToStatusCalculator {

    private static final Set<String> IGNORED_GOCARDLESS_EVENT_ACTIONS = Set.of(
            "customer_approval_granted",
            "customer_approval_skipped",
            "transferred",
            "resubmission_requested"
    );
    private static final Logger LOGGER = LoggerFactory.getLogger(MandateEventToStatusCalculator.class);

    private final GoCardlessEventDao goCardlessEventDao;

    @Inject
    public MandateEventToStatusCalculator(GoCardlessEventDao goCardlessEventDao) {
        this.goCardlessEventDao = goCardlessEventDao;
    }

    public Optional<MandateState> calculate(String goCardlessMandateThingy) {
        return goCardlessEventDao.findEventsForMandateLatestFirst(goCardlessMandateThingy).stream()
                .filter(event -> !IGNORED_GOCARDLESS_EVENT_ACTIONS.contains(event.getAction()))
                .findFirst()
                .flatMap(this::calculateMandateStateFromAction);
    }

    private Optional<MandateState> calculateMandateStateFromAction(Event event) {
        return event.getEventType().getEventActionMapper().calculateMandateStateFromAction(event.getAction());
    }

}
