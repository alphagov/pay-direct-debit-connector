package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class CustomerNotFoundException extends NotFoundException {

    public CustomerNotFoundException(String mandateId, String transactionId) {
        super(format("Customer not found in gocardless, mandate id: %s, transaction id: %s", mandateId, transactionId));
    }
}
