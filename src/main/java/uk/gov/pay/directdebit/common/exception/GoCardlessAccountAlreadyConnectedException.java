package uk.gov.pay.directdebit.common.exception;

public class GoCardlessAccountAlreadyConnectedException extends RuntimeException {
    public GoCardlessAccountAlreadyConnectedException(String message) {
        super(message);
    }
}
