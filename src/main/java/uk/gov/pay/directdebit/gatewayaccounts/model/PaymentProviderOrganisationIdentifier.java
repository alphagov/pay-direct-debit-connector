package uk.gov.pay.directdebit.gatewayaccounts.model;

import java.util.Objects;

public class PaymentProviderOrganisationIdentifier {

    private final String value;

    private PaymentProviderOrganisationIdentifier(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static PaymentProviderOrganisationIdentifier of(String value) {
        return new PaymentProviderOrganisationIdentifier(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == PaymentProviderOrganisationIdentifier.class) {
            PaymentProviderOrganisationIdentifier that = (PaymentProviderOrganisationIdentifier) other;
            return this.value.equals(that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

}
