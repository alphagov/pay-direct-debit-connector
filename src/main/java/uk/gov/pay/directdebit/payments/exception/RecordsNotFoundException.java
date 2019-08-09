package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.directdebit.common.exception.NotFoundException;

public class RecordsNotFoundException extends NotFoundException {
    public RecordsNotFoundException(String message) {
        super(message, ErrorIdentifier.GENERIC);
    }
}
