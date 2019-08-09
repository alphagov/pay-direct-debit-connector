package uk.gov.pay.directdebit.common.exception;

import uk.gov.pay.commons.model.ErrorIdentifier;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message, ErrorIdentifier errorIdentifier) {
        super(message);
    }
}
