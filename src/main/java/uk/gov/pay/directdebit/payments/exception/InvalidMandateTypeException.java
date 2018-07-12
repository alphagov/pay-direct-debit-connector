package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.PreconditionFailedException;
import uk.gov.pay.directdebit.mandate.model.MandateType;

import static java.lang.String.format;

public class InvalidMandateTypeException extends PreconditionFailedException {

    public InvalidMandateTypeException(String externalId, MandateType mandateType) {
        super(format("Invalid operation on mandate with id %s of type %s", externalId, mandateType.toString()));
    }

    public InvalidMandateTypeException(MandateType mandateType) {
        super(format("Invalid operation on mandate of type %s", mandateType.toString()));
    }
}
