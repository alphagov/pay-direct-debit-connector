package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class CustomerNotFoundException extends NotFoundException {

    public CustomerNotFoundException(String paymentRequestExternalId, String mandateId) {
        super(format("Customer not found in gocardless, payment request id: %s, mandate id: %s", paymentRequestExternalId, mandateId));
    }
}
