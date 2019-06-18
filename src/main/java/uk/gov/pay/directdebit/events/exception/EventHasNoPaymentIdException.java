package uk.gov.pay.directdebit.events.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class EventHasNoPaymentIdException extends NotFoundException {

    public EventHasNoPaymentIdException(long id) {
        super(format("Event with id: %s has no linked payment", id));
    }
}
