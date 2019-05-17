package uk.gov.pay.directdebit.gatewayaccounts.model;

import java.util.Objects;

/**
 * An ID that GoCardless assign to a GoCardless account belonging to a
 * service that is connected to our GoCardless partner account e.g. "OR123"
 * <br>
 * GoCardless send this to us in webhooks to indicate which account each
 * event relates to
 * <br>
 * "organisation_id" in GoCardless JSON
 */
public class GoCardlessOrganisationId {

    private final String goCardlessOrganisationId;

    private GoCardlessOrganisationId(String goCardlessOrganisationId) {
        this.goCardlessOrganisationId = Objects.requireNonNull(goCardlessOrganisationId);
    }

    public static GoCardlessOrganisationId valueOf(String paymentProviderAccessToken) {
        return new GoCardlessOrganisationId(paymentProviderAccessToken);
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == GoCardlessOrganisationId.class) {
            GoCardlessOrganisationId that = (GoCardlessOrganisationId) other;
            return this.goCardlessOrganisationId.equals(that.goCardlessOrganisationId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return goCardlessOrganisationId.hashCode();
    }

    @Override
    public String toString() {
        return goCardlessOrganisationId;
    }
    
}
