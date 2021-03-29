package uk.gov.pay.directdebit.common.exception;

import uk.gov.service.payments.commons.model.ErrorIdentifier;

public class PreconditionFailedException extends RuntimeException {

    private ErrorIdentifier errorIdentifier;

    public PreconditionFailedException(String message, ErrorIdentifier errorIdentifier) {
        super(message);
        this.errorIdentifier = errorIdentifier;
    }

    public ErrorIdentifier getErrorIdentifier() {
        return errorIdentifier;
    }
}
