package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class GoCardlessMandateNotConfirmed extends NotFoundException {

    public GoCardlessMandateNotConfirmed(String field, String value) {
        super(format("Mandate with %s: %s has not been confirmed with GoCardless", field, value));
    }
}

