package uk.gov.pay.directdebit.mandate.model.subtype;

import java.util.Objects;

public class MandateExternalId {

    private final String mandateExternalId;

    private MandateExternalId(String mandateExternalId) {
        this.mandateExternalId = Objects.requireNonNull(mandateExternalId);
    }

    public static MandateExternalId valueOf(String mandateExternalId) {
        return new MandateExternalId(mandateExternalId);
    }

    @Override
    public String toString() {
        return mandateExternalId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        MandateExternalId that = (MandateExternalId) other;
        return mandateExternalId.equals(that.mandateExternalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mandateExternalId);
    }

}
