package uk.gov.pay.directdebit.mandate.exception;

import uk.gov.pay.directdebit.common.exception.ConflictException;

public class WrongNumberOfTransactionsForOneOffMandateException extends ConflictException {

    public WrongNumberOfTransactionsForOneOffMandateException(String message) {
        super(message);
    }
}
