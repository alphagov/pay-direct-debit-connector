package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;

import static java.lang.String.format;

public class CreateCustomerBankAccountFailedException extends InternalServerErrorException {

    public CreateCustomerBankAccountFailedException(String paymentRequestExternalId, String payerId) {
        super(format("Failed to create customer bank account in gocardless, payment request id: %s, payer id: %s", paymentRequestExternalId, payerId));
    }
}
