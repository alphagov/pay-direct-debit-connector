package uk.gov.pay.directdebit.common.exception;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(400, message);
    }
}
