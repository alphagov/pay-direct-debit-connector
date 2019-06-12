package uk.gov.pay.directdebit.events.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class EventHasNoMandateIdException extends NotFoundException {

    public EventHasNoMandateIdException(long id) {
        super(format("Event with id: %s has no linked mandate", id));
    }
}
