package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.ConflictException;

import static java.lang.String.format;

public class InvalidStateTransitionException extends ConflictException {

    public InvalidStateTransitionException(String event, String state) {
        super(format("Transition %s from state %s is not valid", event, state));
    }
}
