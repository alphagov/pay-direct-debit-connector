package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class PaymentRequestNotFoundException extends NotFoundException {

    public PaymentRequestNotFoundException(String paymentRequestExternalId) {
        super(format("No payment request found with id: %s", paymentRequestExternalId));
    }
}
