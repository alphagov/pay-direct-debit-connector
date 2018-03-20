package uk.gov.pay.directdebit.mandate.exception;


import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class PayerNotFoundException extends NotFoundException {

    public PayerNotFoundException(String paymentRequestExternalId) {
        super(format("Couldn't find payer for payment request with external id: %s", paymentRequestExternalId));
    }
}
