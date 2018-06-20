package uk.gov.pay.directdebit.payments.exception;

public class UnsupportedDirectDebitEventException extends Exception {

    public UnsupportedDirectDebitEventException(String unsupportedEvent) {
        super(String.format("Event \"%s\" is not supported", unsupportedEvent));
    }
}
