package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.mandate.api.ExternalMandateState;

import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.*;

public enum MandateState {

    PENDING(EXTERNAL_PENDING),
    ACTIVE(EXTERNAL_ACTIVE),
    FAILED(EXTERNAL_INACTIVE),
    CANCELLED(EXTERNAL_INACTIVE);

    private ExternalMandateState externalState;

    MandateState(ExternalMandateState externalState) {
        this.externalState = externalState;
    }

    public ExternalMandateState toExternal() {
        return externalState;
    }
}
