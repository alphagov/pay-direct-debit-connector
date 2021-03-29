package uk.gov.pay.directdebit.events.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;
import uk.gov.pay.directdebit.events.model.GoCardlessEventId;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import static java.lang.String.format;

public class GoCardlessEventHasNoPaymentIdException extends NotFoundException {

    public GoCardlessEventHasNoPaymentIdException(GoCardlessEventId id) {
        super(format("GoCardless event with id: %s has no linked payment", id), ErrorIdentifier.GENERIC);
    }
}
