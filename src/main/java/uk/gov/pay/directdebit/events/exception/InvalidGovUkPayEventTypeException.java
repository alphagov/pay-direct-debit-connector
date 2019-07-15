package uk.gov.pay.directdebit.events.exception;

public class InvalidGovUkPayEventTypeException extends RuntimeException {
    
    public InvalidGovUkPayEventTypeException(String unsupportedEvent) {
        super(String.format("Event \"%s\" is not supported", unsupportedEvent));
    }
}
