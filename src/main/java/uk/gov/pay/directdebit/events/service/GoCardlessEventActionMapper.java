package uk.gov.pay.directdebit.events.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import java.util.Optional;

import static uk.gov.pay.directdebit.mandate.model.MandateState.ACTIVE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CANCELLED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.EXPIRED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.FAILED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED;

public class GoCardlessEventActionMapper implements EventActionMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessEventActionMapper.class);
    
    public Optional<MandateState> calculateMandateStateFromAction(String action) {
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
                LOGGER.error("Received unknown event action " + action);
                return Optional.empty();
        }
    }
}
