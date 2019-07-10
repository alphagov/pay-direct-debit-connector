package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.MandateLookupKey;

import java.util.Objects;

public class GoCardlessMandateIdAndOrganisationId implements MandateLookupKey {

    private final GoCardlessMandateId goCardlessMandateId;
    private final GoCardlessOrganisationId goCardlessOrganisationId;

    public GoCardlessMandateIdAndOrganisationId(GoCardlessMandateId goCardlessMandateId, GoCardlessOrganisationId goCardlessOrganisationId) {
        this.goCardlessMandateId = Objects.requireNonNull(goCardlessMandateId);
        this.goCardlessOrganisationId = Objects.requireNonNull(goCardlessOrganisationId);
    }
    
    public GoCardlessMandateId getGoCardlessMandateId() {
        return goCardlessMandateId;
    }

    public GoCardlessOrganisationId getGoCardlessOrganisationId() {
        return goCardlessOrganisationId;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == GoCardlessMandateIdAndOrganisationId.class) {
            var that = (GoCardlessMandateIdAndOrganisationId) other;
            return this.goCardlessMandateId.equals(that.goCardlessMandateId) && this.goCardlessOrganisationId.equals(that.goCardlessOrganisationId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 37 * goCardlessMandateId.hashCode() ^ goCardlessOrganisationId.hashCode();
    }

    @Override
    public String toString() {
        return "GoCardlessMandateId: " + goCardlessMandateId + ", GoCardlessOrganisationId: " + goCardlessOrganisationId;
    }

}
