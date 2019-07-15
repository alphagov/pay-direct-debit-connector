package uk.gov.pay.directdebit.events.exception;

import static java.lang.String.format;

public class GovUkPayEventHasNoMandateIdException extends RuntimeException {
    public GovUkPayEventHasNoMandateIdException(Long id) {
        super(format("GOV.UK Pay event with id: %s has no linked mandate", id));
    }
}
