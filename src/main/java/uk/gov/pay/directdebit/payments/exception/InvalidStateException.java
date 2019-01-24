package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;

public class InvalidStateException extends InternalServerErrorException {

    public InvalidStateException(String message) {
        super(message);
    }
}

