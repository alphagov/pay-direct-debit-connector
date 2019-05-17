package uk.gov.pay.directdebit.payments.model;

import java.util.Objects;

/**
 * The ID assigned by GoCardless to one of their events e.g. "EV123"
 * 
 * @see <a href="https://developer.gocardless.com/api-reference/#core-endpoints-events">GoCardless Events</a>
 */
public class GoCardlessEventId {

    private final String goCardlessEventId;

    private GoCardlessEventId(String goCardlessEventId) {
        this.goCardlessEventId = Objects.requireNonNull(goCardlessEventId);
    }

    public static GoCardlessEventId valueOf(String goCardlessEventId) {
        return new GoCardlessEventId(goCardlessEventId);
    }

    @Override
    public String toString() {
        return goCardlessEventId;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == GoCardlessEventId.class) {
            GoCardlessEventId that = (GoCardlessEventId) other;
            return this.goCardlessEventId.equals(that.goCardlessEventId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return goCardlessEventId.hashCode();
    }

}
