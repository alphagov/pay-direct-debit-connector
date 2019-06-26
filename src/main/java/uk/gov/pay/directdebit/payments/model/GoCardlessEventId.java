package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.commons.model.WrappedStringValue;

/**
 * The ID assigned by GoCardless to one of their events e.g. "EV123"
 * 
 * @see <a href="https://developer.gocardless.com/api-reference/#core-endpoints-events">GoCardless Events</a>
 */
public class GoCardlessEventId extends WrappedStringValue {

    private GoCardlessEventId(String goCardlessEventId) {
        super(goCardlessEventId);
    }

    public static GoCardlessEventId valueOf(String goCardlessEventId) {
        return new GoCardlessEventId(goCardlessEventId);
    }

}
