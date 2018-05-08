package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;

import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_CANCELLED_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_FAILED;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_PENDING;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_STARTED;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_SUCCESS;

public enum PaymentState {

    NEW(EXTERNAL_STARTED),

    AWAITING_DIRECT_DEBIT_DETAILS(EXTERNAL_STARTED),
    SUBMITTING_DIRECT_DEBIT_PAYMENT(EXTERNAL_STARTED),
    PENDING_DIRECT_DEBIT_PAYMENT(EXTERNAL_PENDING),
    FAILED(EXTERNAL_FAILED),
    CANCELLED(EXTERNAL_FAILED),
    USER_CANCEL_NOT_ELIGIBLE(EXTERNAL_CANCELLED_USER_NOT_ELIGIBLE),

    SUCCESS(EXTERNAL_SUCCESS);

    private ExternalPaymentState externalState;

    PaymentState(ExternalPaymentState externalState) {
        this.externalState = externalState;
    }

    public ExternalPaymentState toExternal() {
        return externalState;
    }
}
