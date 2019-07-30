package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.mandate.api.ExternalMandateState;
import uk.gov.pay.directdebit.payments.model.DirectDebitState;

import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.ABANDONED;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.INACTIVE;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.PENDING;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.STARTED;

public enum MandateState implements DirectDebitState {
    CREATED(ExternalMandateState.CREATED),
    AWAITING_DIRECT_DEBIT_DETAILS(STARTED),
    SUBMITTED_TO_PROVIDER(PENDING),
    SUBMITTED_TO_BANK(PENDING),
    ACTIVE(ExternalMandateState.ACTIVE),
    FAILED(ExternalMandateState.FAILED),
    EXPIRED(INACTIVE),
    CANCELLED(ExternalMandateState.CANCELLED),
    USER_SETUP_CANCELLED(ABANDONED),
    USER_SETUP_EXPIRED(ABANDONED);

    private ExternalMandateState externalState;

    MandateState(ExternalMandateState externalState) {
        this.externalState = externalState;
    }

    public ExternalMandateState toExternal() {
        return externalState;
    }
}
