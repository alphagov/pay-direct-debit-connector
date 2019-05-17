package uk.gov.pay.directdebit.mandate.model;

import java.util.Objects;

/**
 * The ID assigned by GoCardless to a Direct Debit mandate e.g. "MD123"
 * 
 * @see <a href="https://developer.gocardless.com/api-reference/#core-endpoints-mandates">GoCardless Mandates</a>
 */
public class GoCardlessMandateId {
    
    private final String goCardlessMandateId;

    private GoCardlessMandateId(String goCardlessEventId) {
        this.goCardlessMandateId = Objects.requireNonNull(goCardlessEventId);
    }

    public static GoCardlessMandateId valueOf(String goCardlessMandateId) {
        return new GoCardlessMandateId(goCardlessMandateId);
    }

    @Override
    public String toString() {
        return goCardlessMandateId;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == GoCardlessMandateId.class) {
            GoCardlessMandateId that = (GoCardlessMandateId) other;
            return this.goCardlessMandateId.equals(that.goCardlessMandateId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return goCardlessMandateId.hashCode();
    }
    
}
