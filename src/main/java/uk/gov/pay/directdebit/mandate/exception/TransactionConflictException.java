package uk.gov.pay.directdebit.mandate.exception;

import uk.gov.pay.directdebit.common.exception.ConflictException;

public class TransactionConflictException extends ConflictException {

    public TransactionConflictException(String message) {
        super(message);
    }
}
