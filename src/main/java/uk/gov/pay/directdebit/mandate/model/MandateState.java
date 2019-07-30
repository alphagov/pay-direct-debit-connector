package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.mandate.api.ExternalMandateState;
import uk.gov.pay.directdebit.payments.model.DirectDebitState;

import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.EXTERNAL_ABANDONED;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.EXTERNAL_ACTIVE;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.EXTERNAL_CANCELLED;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.EXTERNAL_CREATED;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.EXTERNAL_FAILED;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.EXTERNAL_INACTIVE;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.EXTERNAL_PENDING;
import static uk.gov.pay.directdebit.mandate.api.ExternalMandateState.EXTERNAL_STARTED;

public enum MandateState implements DirectDebitState {
    CREATED(EXTERNAL_CREATED),
    AWAITING_DIRECT_DEBIT_DETAILS(EXTERNAL_STARTED),
    SUBMITTED_TO_PROVIDER(EXTERNAL_PENDING),
    SUBMITTED_TO_BANK(EXTERNAL_PENDING),
    ACTIVE(EXTERNAL_ACTIVE),
    FAILED(EXTERNAL_FAILED),
    EXPIRED(EXTERNAL_INACTIVE),
    CANCELLED(EXTERNAL_CANCELLED),
    USER_SETUP_CANCELLED(EXTERNAL_ABANDONED),
    USER_SETUP_EXPIRED(EXTERNAL_ABANDONED);

    private ExternalMandateState externalState;

    MandateState(ExternalMandateState externalState) {
        this.externalState = externalState;
    }

    public ExternalMandateState toExternal() {
        return externalState;
    }
}
