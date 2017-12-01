package uk.gov.pay.directdebit.payments.exception;

public class UnsupportedPaymentRequestEventException extends Exception {

    public UnsupportedPaymentRequestEventException(String unsupportedEvent) {
        super(String.format("Event \"%s\" is not supported", unsupportedEvent));
    }
}
