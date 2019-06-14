package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;

import static java.lang.String.format;

public class CreatePaymentFailedException extends InternalServerErrorException {

    public CreatePaymentFailedException(String mandateExternalId, String paymentExternalId) {
        super(format("Failed to create payment in gocardless, mandate id: %s, payment id: %s", mandateExternalId, paymentExternalId));
    }
}
