package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class GoCardlessPaymentNotFoundException extends NotFoundException {

    public GoCardlessPaymentNotFoundException(String goCardlessResourceId) {
        super(format("No gocardless payment found with resource id: %s", goCardlessResourceId));
    }
}

