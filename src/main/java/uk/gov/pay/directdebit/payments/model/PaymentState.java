package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;

import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.*;

public enum PaymentState {
    NEW(EXTERNAL_STARTED),
    AWAITING_DIRECT_DEBIT_DETAILS(EXTERNAL_STARTED);

    private ExternalPaymentState externalState;

    PaymentState(ExternalPaymentState externalState) {
        this.externalState = externalState;
    }
    public ExternalPaymentState toExternal() {
        return externalState;
    }
}
