package uk.gov.pay.directdebit.payments.exception;

import static java.lang.String.*;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String event, String state) {
        super(format("Transition %s from state %s is not valid", event, state));
    }
}
