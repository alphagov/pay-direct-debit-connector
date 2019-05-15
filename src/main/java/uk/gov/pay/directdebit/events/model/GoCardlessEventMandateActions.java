package uk.gov.pay.directdebit.events.model;

import uk.gov.pay.directdebit.mandate.model.MandateState;

public enum GoCardlessEventActions {
    CREATED(MandateState.CREATED),
    CUSTOMER_APPROVAL_SKIPPED(MandateState.PENDING),
    SUBMITTED(MandateState.SUBMITTED),
    ACTIVE(MandateState.ACTIVE),
    REINSTATED(MandateState.ACTIVE),
    TRANSFERRED(MandateState.ACTIVE),
    CANCELLED(MandateState.CANCELLED),
    FAILED(MandateState.FAILED),
    EXPIRED(MandateState.EXPIRED),
    RESUBMISSION_REQUESTED(MandateState.PENDING),
    REPLACED(MandateState.ACTIVE);

    private MandateState mandateState;

    GoCardlessEventActions(MandateState mandateState) {
        this.mandateState = mandateState;
    }

    public MandateState getMandateState() {
        return mandateState;
    };
}
