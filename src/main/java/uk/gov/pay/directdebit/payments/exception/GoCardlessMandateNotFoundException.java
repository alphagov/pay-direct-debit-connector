package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class GoCardlessMandateNotFoundException extends NotFoundException {

    public GoCardlessMandateNotFoundException(String goCardlessResourceId) {
        super(format("No gocardless mandate found with resource id: %s", goCardlessResourceId));
    }
}

