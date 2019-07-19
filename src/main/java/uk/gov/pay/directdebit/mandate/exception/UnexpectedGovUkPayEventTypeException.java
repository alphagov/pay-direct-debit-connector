package uk.gov.pay.directdebit.mandate.exception;

import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.mandate.model.Mandate;

import static java.lang.String.format;

public class UnexpectedGovUkPayEventTypeException extends RuntimeException {
    public UnexpectedGovUkPayEventTypeException(GovUkPayEvent.GovUkPayEventType eventType, Mandate mandate) {
        super(format("Unhandled GOV.UK Pay event type %s when calculating state for mandate %s", eventType, mandate.getExternalId()));
    }
}
