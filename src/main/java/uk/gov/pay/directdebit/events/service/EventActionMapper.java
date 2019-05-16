package uk.gov.pay.directdebit.events.service;

import uk.gov.pay.directdebit.mandate.model.MandateState;

import java.util.Optional;

public interface EventActionMapper {

    Optional<MandateState> calculateMandateStateFromAction(String action);
}
