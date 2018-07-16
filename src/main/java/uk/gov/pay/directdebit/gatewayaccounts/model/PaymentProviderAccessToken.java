package uk.gov.pay.directdebit.gatewayaccounts.model;

import java.util.Objects;

public class PaymentProviderAccessToken {

    private final String paymentProviderAccessToken;

    private PaymentProviderAccessToken(String paymentProviderAccessToken) {
        this.paymentProviderAccessToken = Objects.requireNonNull(paymentProviderAccessToken);
    }

    public static PaymentProviderAccessToken of(String paymentProviderAccessToken) {
        return new PaymentProviderAccessToken(paymentProviderAccessToken);
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == PaymentProviderAccessToken.class) {
            PaymentProviderAccessToken that = (PaymentProviderAccessToken) other;
            return this.paymentProviderAccessToken.equals(that.paymentProviderAccessToken);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return paymentProviderAccessToken.hashCode();
    }

    @Override
    public String toString() {
        return paymentProviderAccessToken;
    }
    
}
