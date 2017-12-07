package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;

import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.*;

public enum PaymentState {
    NEW(EXTERNAL_STARTED),
    ENTER_DIRECT_DEBIT_DETAILS(EXTERNAL_STARTED),
    ENTER_DIRECT_DEBIT_DETAILS_FAILED(EXTERNAL_FAILED),
    ENTER_DIRECT_DEBIT_DETAILS_ERROR(EXTERNAL_FAILED),
    CONFIRM_DIRECT_DEBIT_DETAILS(EXTERNAL_STARTED),
    CONFIRM_DIRECT_DEBIT_DETAILS_FAILED(EXTERNAL_FAILED),
    CONFIRM_DIRECT_DEBIT_DETAILS_ERROR(EXTERNAL_FAILED),
    REQUESTED(EXTERNAL_PENDING),
    REQUESTED_FAILED(EXTERNAL_FAILED),
    REQUESTED_ERROR(EXTERNAL_FAILED),
    IN_PROGRESS(EXTERNAL_PENDING),
    IN_PROGRESS_FAILED(EXTERNAL_FAILED),
    PROVIDER_CANCELLED(EXTERNAL_CANCELLED),
    EXPIRED(EXTERNAL_EXPIRED),
    USER_CANCELLED(EXTERNAL_CANCELLED),
    SYSTEM_CANCELLED(EXTERNAL_CANCELLED),
    SUCCESS(EXTERNAL_SUCCESS),
    PAID_OUT(EXTERNAL_SUCCESS);

    private ExternalPaymentState externalState;

    PaymentState(ExternalPaymentState externalState) {
        this.externalState = externalState;
    }
    public ExternalPaymentState toExternal() {
        return externalState;
    }
}
