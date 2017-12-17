package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;

import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_PENDING;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_STARTED;

public enum PaymentState {
    NEW(EXTERNAL_STARTED),
    AWAITING_DIRECT_DEBIT_DETAILS(EXTERNAL_STARTED),
    PROCESSING_DIRECT_DEBIT_DETAILS(EXTERNAL_STARTED),
    AWAITING_CONFIRMATION(EXTERNAL_STARTED),
    CONFIRMED_DIRECT_DEBIT_DETAILS(EXTERNAL_PENDING);

    private ExternalPaymentState externalState;

    PaymentState(ExternalPaymentState externalState) {
        this.externalState = externalState;
    }
    public ExternalPaymentState toExternal() {
        return externalState;
    }
}
