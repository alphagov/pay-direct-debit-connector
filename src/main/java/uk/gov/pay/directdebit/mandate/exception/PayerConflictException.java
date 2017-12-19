package uk.gov.pay.directdebit.mandate.exception;

import uk.gov.pay.directdebit.common.exception.ConflictException;

public class PayerConflictException extends ConflictException {

    public PayerConflictException(String message) {
        super(message);
    }
}
