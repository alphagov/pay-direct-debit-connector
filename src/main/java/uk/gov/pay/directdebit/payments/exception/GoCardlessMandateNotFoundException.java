package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import static java.lang.String.format;

public class GoCardlessMandateNotFoundException extends NotFoundException {

    public GoCardlessMandateNotFoundException(String field, String value) {
        super(format("No gocardless mandate found with %s: %s", field, value), ErrorIdentifier.GENERIC);
    }
}

