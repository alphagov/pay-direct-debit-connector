package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class ChargeNotFoundException extends NotFoundException {

    public ChargeNotFoundException(String paymentRequestExternalId) {
        super(format("No charges found for payment request with id: %s", paymentRequestExternalId));
    }
}
