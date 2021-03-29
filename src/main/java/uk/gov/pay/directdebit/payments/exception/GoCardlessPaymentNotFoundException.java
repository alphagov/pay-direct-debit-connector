package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import static java.lang.String.format;

public class GoCardlessPaymentNotFoundException extends NotFoundException {

    public GoCardlessPaymentNotFoundException(String goCardlessResourceId) {
        super(format("No gocardless payment found with resource id: %s", goCardlessResourceId),
                ErrorIdentifier.GENERIC);
    }
}

