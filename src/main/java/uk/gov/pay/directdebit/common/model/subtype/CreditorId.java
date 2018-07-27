package uk.gov.pay.directdebit.common.model.subtype;

import java.util.Objects;

public class CreditorId {

    private final String creditorId;

    private CreditorId(String creditorId) {
        this.creditorId = Objects.requireNonNull(creditorId);
    }

    public static CreditorId of(String mandateExternalId) {
        return new CreditorId(mandateExternalId);
    }

    public static CreditorId valueOf(String creditorId) {
        return CreditorId.of(creditorId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreditorId that = (CreditorId) o;
        return Objects.equals(creditorId, that.creditorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creditorId);
    }

    @Override
    public String toString() {
        return creditorId;
    }
}
