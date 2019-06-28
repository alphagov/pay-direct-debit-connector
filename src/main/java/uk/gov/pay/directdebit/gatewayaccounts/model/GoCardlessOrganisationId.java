package uk.gov.pay.directdebit.gatewayaccounts.model;

/**
 * An ID that GoCardless assign to a GoCardless account belonging to a
 * service that is connected to our GoCardless partner app e.g. "OR123"
 * <br>
 * GoCardless send this to us in webhooks to indicate which service each
 * event relates to
 * <br>
 * {@code "organisation"} or {@code "organisation_id"} in GoCardless JSON
 */
public class GoCardlessOrganisationId extends PaymentProviderServiceId {

    private GoCardlessOrganisationId(String goCardlessOrganisationId) {
        super(goCardlessOrganisationId);
    }

    public static GoCardlessOrganisationId valueOf(String goCardlessOrganisationId) {
        return new GoCardlessOrganisationId(goCardlessOrganisationId);
    }

}
