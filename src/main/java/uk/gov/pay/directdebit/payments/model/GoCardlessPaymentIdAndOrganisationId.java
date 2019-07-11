package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;

import java.util.Objects;

public class GoCardlessPaymentIdAndOrganisationId implements PaymentLookupKey {

    private final GoCardlessPaymentId goCardlessPaymentId;
    private final GoCardlessOrganisationId goCardlessOrganisationId;

    public GoCardlessPaymentIdAndOrganisationId(GoCardlessPaymentId goCardlessPaymentId, GoCardlessOrganisationId goCardlessOrganisationId) {
        this.goCardlessPaymentId = Objects.requireNonNull(goCardlessPaymentId);
        this.goCardlessOrganisationId = Objects.requireNonNull(goCardlessOrganisationId);
    }
    
    public GoCardlessPaymentId getGoCardlessPaymentId() {
        return goCardlessPaymentId;
    }

    public GoCardlessOrganisationId getGoCardlessOrganisationId() {
        return goCardlessOrganisationId;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == GoCardlessPaymentIdAndOrganisationId.class) {
            var that = (GoCardlessPaymentIdAndOrganisationId) other;
            return this.goCardlessPaymentId.equals(that.goCardlessPaymentId) && this.goCardlessOrganisationId.equals(that.goCardlessOrganisationId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 37 * goCardlessPaymentId.hashCode() ^ goCardlessOrganisationId.hashCode();
    }

    @Override
    public String toString() {
        return "GoCardlessPaymentId: " + goCardlessPaymentId + ", GoCardlessOrganisationId: " + goCardlessOrganisationId;
    }

}
