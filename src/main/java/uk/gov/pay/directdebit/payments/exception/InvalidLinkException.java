package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.BadRequestException;

public class InvalidLinkException extends BadRequestException {
    public InvalidLinkException(String message) {
        super(message);
    }
}
