package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.mandate.api.ExternalMandateState;
import uk.gov.pay.directdebit.payments.model.DirectDebitState;

import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.INACTIVE;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.STARTED;


public enum MandateState implements DirectDebitState {
    CREATED(ExternalMandateState.CREATED),
    AWAITING_DIRECT_DEBIT_DETAILS(STARTED),
    USER_CANCEL_NOT_ELIGIBLE(ExternalMandateState.CANCELLED),
    SUBMITTED_TO_PROVIDER(ExternalMandateState.PENDING),
    PENDING(ExternalMandateState.PENDING),
    ACTIVE(ExternalMandateState.ACTIVE),
    FAILED(INACTIVE),
    CANCELLED(INACTIVE),
    EXPIRED(INACTIVE);

    private ExternalMandateState externalState;

    MandateState(ExternalMandateState externalState) {
        this.externalState = externalState;
    }

    public ExternalMandateState toExternal() {
        return externalState;
    }
}
