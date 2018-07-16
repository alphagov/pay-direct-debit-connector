package uk.gov.pay.directdebit.gatewayaccounts.model;

import java.util.Objects;

public class PaymentProviderOrganisationIdentifier {

    private final String paymentProviderOrganisationIdentifier;

    private PaymentProviderOrganisationIdentifier(String paymentProviderOrganisationIdentifier) {
        this.paymentProviderOrganisationIdentifier = Objects.requireNonNull(paymentProviderOrganisationIdentifier);
    }

    public static PaymentProviderOrganisationIdentifier of(String paymentProviderAccessToken) {
        return new PaymentProviderOrganisationIdentifier(paymentProviderAccessToken);
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == PaymentProviderOrganisationIdentifier.class) {
            PaymentProviderOrganisationIdentifier that = (PaymentProviderOrganisationIdentifier) other;
            return this.paymentProviderOrganisationIdentifier.equals(that.paymentProviderOrganisationIdentifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return paymentProviderOrganisationIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return paymentProviderOrganisationIdentifier;
    }
    
}
