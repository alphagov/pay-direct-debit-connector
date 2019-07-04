package uk.gov.pay.directdebit.mandate.exception;

import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;

public class MandateStateInvalidException extends InternalServerErrorException {
    public MandateStateInvalidException(String message) {
        super(message);
    }
}
