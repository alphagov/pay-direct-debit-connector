package uk.gov.pay.directdebit.mandate.model.subtype;

import java.util.Objects;

public class MandateExternalId {

    private final String mandateExternalId;

    private MandateExternalId(String mandateExternalId) {
        this.mandateExternalId = Objects.requireNonNull(mandateExternalId);
    }

    public static MandateExternalId of(String mandateExternalId) {
        return new MandateExternalId(mandateExternalId);
    }

    public static MandateExternalId valueOf(String mandateExternalId) {
        return MandateExternalId.of(mandateExternalId);
    }

    public String getMandateExternalId() {
        return mandateExternalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MandateExternalId that = (MandateExternalId) o;
        return Objects.equals(mandateExternalId, that.mandateExternalId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(mandateExternalId);
    }

    @Override
    public String toString() {
        return mandateExternalId;
    }

}
