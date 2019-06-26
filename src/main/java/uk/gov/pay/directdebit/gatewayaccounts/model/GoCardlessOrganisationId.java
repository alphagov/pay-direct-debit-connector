package uk.gov.pay.directdebit.gatewayaccounts.model;

import uk.gov.pay.commons.model.WrappedStringValue;

/**
 * An ID that GoCardless assign to a GoCardless account belonging to a
 * service that is connected to our GoCardless partner account e.g. "OR123"
 * <br>
 * GoCardless send this to us in webhooks to indicate which account each
 * event relates to
 * <br>
 * "organisation" or "organisation_id" in GoCardless JSON
 */
public class GoCardlessOrganisationId extends WrappedStringValue {

    private GoCardlessOrganisationId(String goCardlessOrganisationId) {
        super(goCardlessOrganisationId);
    }

    public static GoCardlessOrganisationId valueOf(String goCardlessOrganisationId) {
        return new GoCardlessOrganisationId(goCardlessOrganisationId);
    }

}
