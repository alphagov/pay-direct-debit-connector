package uk.gov.pay.directdebit.events.exception;

import uk.gov.pay.directdebit.events.model.GovUkPayEvent;

import static java.lang.String.format;

public class InvalidGovUkPayEventInsertionException extends RuntimeException {
    public InvalidGovUkPayEventInsertionException(GovUkPayEvent previousEvent, GovUkPayEvent newEvent) {
        super(format("GOV.UK Pay event {} is invalid following event {}", newEvent.getEventType(), previousEvent.getEventType()));
    }
    
    public InvalidGovUkPayEventInsertionException(GovUkPayEvent event) {
        super(format("GOV.UK Pay event {} is invalid when there are no previous events", event.getEventType()));
    }
}
