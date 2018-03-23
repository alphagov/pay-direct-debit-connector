package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;
import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class InvalidStateException extends InternalServerErrorException {

    public InvalidStateException(String message) {
        super(message);
    }
}

