package uk.gov.pay.directdebit.mandate.exception;

import uk.gov.pay.directdebit.mandate.model.Mandate;

import static java.lang.String.format;

public class UnexpectedGoCardlessEventActionException extends RuntimeException {
    public UnexpectedGoCardlessEventActionException(String action, Mandate mandate) {
        super(format("Unhandled GoCardless event action %s when calculating state for mandate %s", action, mandate.getExternalId()));
    }
}
