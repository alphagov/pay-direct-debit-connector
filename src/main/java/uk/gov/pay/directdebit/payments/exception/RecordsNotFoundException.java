package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

public class RecordsNotFoundException extends NotFoundException {
    public RecordsNotFoundException(String message) {
        super(message, ErrorIdentifier.GENERIC);
    }
}
