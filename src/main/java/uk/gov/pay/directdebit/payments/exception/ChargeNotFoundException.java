package uk.gov.pay.directdebit.payments.exception;

import static java.lang.String.format;

public class ChargeNotFoundException extends RuntimeException {

    public ChargeNotFoundException(String paymentRequestExternalId) {
        super(format("No charges found for payment request with id: %s", paymentRequestExternalId));
    }
}
