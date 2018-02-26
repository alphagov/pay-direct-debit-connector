package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;

import static java.lang.String.format;

public class CreateMandateFailedException extends InternalServerErrorException {

    public CreateMandateFailedException(String paymentRequestExternalId, String mandateId) {
        super(format("Failed to create mandate in gocardless, payment request id: %s, mandate id: %s", paymentRequestExternalId, mandateId));
    }
}
