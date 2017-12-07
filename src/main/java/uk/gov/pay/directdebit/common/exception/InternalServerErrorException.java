package uk.gov.pay.directdebit.common.exception;

public class InternalServerErrorException extends ApiException {

    public InternalServerErrorException(String message) {
        super(500, message);
    }
}
