package uk.gov.pay.directdebit.payments.model;

import java.util.Objects;

public abstract class PaymentProviderPaymentId {

    private final String paymentProviderId;

    PaymentProviderPaymentId(String paymentProviderId) {
        this.paymentProviderId = Objects.requireNonNull(paymentProviderId);
    }

    @Override
    public String toString() {
        return paymentProviderId;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null) {
            PaymentProviderPaymentId that = (PaymentProviderPaymentId) other;
            return this.paymentProviderId.equals(that.paymentProviderId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return paymentProviderId.hashCode();
    }
}
