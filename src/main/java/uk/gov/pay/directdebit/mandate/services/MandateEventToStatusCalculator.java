package uk.gov.pay.directdebit.mandate.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.mandate.model.MandateState.ACTIVE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CANCELLED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.EXPIRED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.FAILED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED;

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

    public Optional<MandateState> calculate(MandateExternalId mandateExternalId) {
        return goCardlessEventDao.findEventsForMandateLatestFirst(mandateExternalId.toString()).stream()
                .filter(event -> !IGNORED_GOCARDLESS_EVENT_ACTIONS.contains(event.getAction()))
                .findFirst()
                .flatMap(event -> calculateMandateStateFromAction(mandateExternalId, event.getAction()));
    }

    private Optional<MandateState> calculateMandateStateFromAction(MandateExternalId mandateExternalId, String action) {
        switch (action) {
            case "created":
                return Optional.of(CREATED);
            case "submitted":
                return Optional.of(SUBMITTED);
            case "active":
                return Optional.of(ACTIVE);
            case "cancelled":
                return Optional.of(CANCELLED);
            case "failed":
                return Optional.of(FAILED);
            case "expired":
                return Optional.of(EXPIRED);
            default:
                LOGGER.error("Received unknown event action " + action + " for mandate " + mandateExternalId);
                return Optional.empty();
        }
    }

}
