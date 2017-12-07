package uk.gov.pay.directdebit.common.exception;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(404, message);
    }
}
