package uk.gov.pay.directdebit.common.exception;

import uk.gov.pay.commons.model.ErrorIdentifier;

public class NotFoundException extends RuntimeException {

    private ErrorIdentifier errorIdentifier;
    
    public NotFoundException(String message, ErrorIdentifier errorIdentifier) {
        super(message);
        this.errorIdentifier = errorIdentifier;
    }

    public ErrorIdentifier getErrorIdentifier() {
        return errorIdentifier;
    }
}
