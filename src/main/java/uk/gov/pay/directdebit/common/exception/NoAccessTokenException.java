package uk.gov.pay.directdebit.common.exception;

public class NoAccessTokenException extends RuntimeException {
    public NoAccessTokenException(String message){
        super(message);
    }
}
