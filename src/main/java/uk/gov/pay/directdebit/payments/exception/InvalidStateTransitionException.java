package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;

import static java.lang.String.*;

public class InvalidStateTransitionException extends InternalServerErrorException {

    public InvalidStateTransitionException(String event, String state) {
        super(format("Transition %s from state %s is not valid", event, state));
    }
}
