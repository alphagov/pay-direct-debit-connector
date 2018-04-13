package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class ChargeNotFoundException extends NotFoundException {

    public ChargeNotFoundException(String key, String id) {
        super(format("No charges found for %s: %s", key, id));
    }
}
