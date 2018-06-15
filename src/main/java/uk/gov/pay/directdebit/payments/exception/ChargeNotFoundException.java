package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

public class ChargeNotFoundException extends NotFoundException {

    public ChargeNotFoundException(String message) {
        super(message);
    }
}
