package uk.gov.pay.directdebit.common.model.subtype.gocardless.creditor;

import java.util.Objects;

public class GoCardlessCreditorId {

    private final String goCardlessCreditorId;

    private GoCardlessCreditorId(String goCardlessCreditorId) {
        this.goCardlessCreditorId = Objects.requireNonNull(goCardlessCreditorId);
    }

    public static GoCardlessCreditorId valueOf(String goCardlessCreditorId) {
        return new GoCardlessCreditorId(goCardlessCreditorId);
    }

    @Override
    public String toString() {
        return goCardlessCreditorId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        GoCardlessCreditorId that = (GoCardlessCreditorId) other;
        return goCardlessCreditorId.equals(that.goCardlessCreditorId);
    }

    @Override
    public int hashCode() {
        return goCardlessCreditorId.hashCode();
    }

}
