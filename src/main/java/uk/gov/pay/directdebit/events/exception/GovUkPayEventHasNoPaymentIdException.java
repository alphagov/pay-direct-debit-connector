package uk.gov.pay.directdebit.events.exception;

import static java.lang.String.format;

public class GovUkPayEventHasNoPaymentIdException extends RuntimeException {
    public GovUkPayEventHasNoPaymentIdException(Long id) {
        super(format("GOV.UK Pay event with id: %s has no linked mandate", id));
    }
}
