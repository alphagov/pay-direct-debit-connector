package uk.gov.pay.directdebit.common.exception;

import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import static java.lang.String.format;

public class MandateIdInvalidException extends RuntimeException {
    
    public MandateIdInvalidException(MandateExternalId mandateExternalId, String message) {
        super(format("Mandate with ID: %s not found", mandateExternalId));
    }
}
