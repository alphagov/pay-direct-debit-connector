package uk.gov.pay.directdebit.gatewayaccounts.exception;

import uk.gov.pay.directdebit.common.exception.BadRequestException;

public class InvalidPayloadException extends BadRequestException {
    public InvalidPayloadException(String msg) {
        super(msg);
    }
}
