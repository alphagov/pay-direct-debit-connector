package uk.gov.pay.directdebit.events.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.model.Event;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MandateEventActionToStatusCalculator {

    private static final Set<String> IGNORED_GOCARDLESS_EVENT_ACTIONS = Set.of(
            "customer_approval_granted",
            "customer_approval_skipped",
            "transferred",
            "resubmission_requested"
    );
    private static final Logger LOGGER = LoggerFactory.getLogger(MandateEventActionToStatusCalculator.class);

    private final GoCardlessEventDao goCardlessEventDao;
    private final GovUkPayEventDao govUkPayEventDao;

    @Inject
    public MandateEventActionToStatusCalculator(GoCardlessEventDao goCardlessEventDao, GovUkPayEventDao govUkPayEventDao) {
        this.goCardlessEventDao = goCardlessEventDao;
        this.govUkPayEventDao = govUkPayEventDao;
    }

    public Optional<MandateState> calculate(Mandate mandate) {
        List<GoCardlessEvent> goCardlessEvents = goCardlessEventDao.findEventsForMandateLatestFirst(mandate.getMandateReference());
        List<GovUkPayEvent> govUkPayEvents = govUkPayEventDao.findEventsForMandateLatestFirst(mandate.getExternalId().toString());
        
        List<Event> allEvents = new ArrayList<>();
        allEvents.addAll(goCardlessEvents);
        allEvents.addAll(govUkPayEvents);
        
        return allEvents.stream()
                .filter(event -> !IGNORED_GOCARDLESS_EVENT_ACTIONS.contains(event.getAction()))
                .findFirst()
                .flatMap(this::calculateMandateStateFromAction);
    }

    public Optional<MandateState> calculate(MandateExternalId mandateExternalId) {
        return govUkPayEventDao.findLatestEventForMandate(mandateExternalId.toString()).flatMap(this::calculateMandateStateFromAction);
    }

    private Optional<MandateState> calculateMandateStateFromAction(Event event) {
        return event.getEventType().getEventActionMapper().calculateMandateStateFromAction(event.getAction());
    }

}
