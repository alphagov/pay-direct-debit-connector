package uk.gov.pay.directdebit.common.model.subtype.gocardless.creditor;

import java.util.Objects;

public class GoCardlessCreditorId {

    private final String goCardlessCreditorId;

    private GoCardlessCreditorId(String goCardlessCreditorId) {
        this.goCardlessCreditorId = Objects.requireNonNull(goCardlessCreditorId);
    }

    public static GoCardlessCreditorId of(String goCardlessCreditorId) {
        return new GoCardlessCreditorId(goCardlessCreditorId);
    }

    public static GoCardlessCreditorId valueOf(String goCardlessCreditorId) {
        return GoCardlessCreditorId.of(goCardlessCreditorId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoCardlessCreditorId that = (GoCardlessCreditorId) o;

        return goCardlessCreditorId.equals(that.goCardlessCreditorId);
    }

    @Override
    public int hashCode() {
        return goCardlessCreditorId.hashCode();
    }

    @Override
    public String toString() {
        return goCardlessCreditorId;
    }
}
