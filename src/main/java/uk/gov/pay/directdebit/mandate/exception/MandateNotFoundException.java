package uk.gov.pay.directdebit.mandate.exception;


import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class MandateNotFoundException extends NotFoundException {

    public MandateNotFoundException(String transactionId) {
        super(format("Couldn't find mandate for transaction with id: %s", transactionId));
    }
}
