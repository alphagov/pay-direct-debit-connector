package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;

import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_CANCELLED;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_CREATED;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_ERROR;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_FAILED;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_INDEMNITY_CLAIM;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_PAID_OUT;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_PENDING;
import static uk.gov.pay.directdebit.payments.api.ExternalPaymentState.EXTERNAL_SUCCESS;

public enum PaymentState implements DirectDebitState {

    CREATED(EXTERNAL_CREATED),
    SUBMITTED_TO_PROVIDER(EXTERNAL_PENDING),
    FAILED(EXTERNAL_FAILED),
    CANCELLED(EXTERNAL_CANCELLED),
    PAID_OUT(EXTERNAL_PAID_OUT),
    CUSTOMER_APPROVAL_DENIED(EXTERNAL_FAILED),
    SUBMITTED_TO_BANK(EXTERNAL_PENDING),
    COLLECTED_BY_PROVIDER(EXTERNAL_SUCCESS),
    INDEMNITY_CLAIM(EXTERNAL_INDEMNITY_CLAIM),
    PROVIDER_ERROR(EXTERNAL_ERROR);

    private ExternalPaymentState externalState;

    PaymentState(ExternalPaymentState externalState) {
        this.externalState = externalState;
    }

    public ExternalPaymentState toExternal() {
        return externalState;
    }

    public String toSingleQuoteString() {
        return "'" + this + "'";
    }
}
