package uk.gov.pay.directdebit.mandate.model;

import java.util.Objects;

public abstract class PaymentProviderMandateId {

    private final String paymentProviderId;

    PaymentProviderMandateId(String paymentProviderId) {
        this.paymentProviderId = Objects.requireNonNull(paymentProviderId);
    }

    @Override
    public String toString() {
        return paymentProviderId;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null) {
            PaymentProviderMandateId that = (PaymentProviderMandateId) other;
            return this.paymentProviderId.equals(that.paymentProviderId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return paymentProviderId.hashCode();
    }
}
