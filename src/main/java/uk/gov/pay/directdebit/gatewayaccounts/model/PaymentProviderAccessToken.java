package uk.gov.pay.directdebit.gatewayaccounts.model;

import java.util.Objects;

public class PaymentProviderAccessToken {

    private final String value;

    private PaymentProviderAccessToken(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static PaymentProviderAccessToken of(String value) {
        return new PaymentProviderAccessToken(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == PaymentProviderAccessToken.class) {
            PaymentProviderAccessToken that = (PaymentProviderAccessToken) other;
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
