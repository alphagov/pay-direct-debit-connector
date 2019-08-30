package uk.gov.pay.directdebit.common.exception;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(String message) {
        super(message);
    }

    public InternalServerErrorException(String message, Exception cause) {
        super(message, cause);
    }
}
