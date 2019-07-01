package uk.gov.pay.directdebit.mandate.model;

/**
 * The ID assigned by GoCardless to a Direct Debit mandate e.g. "MD123"
 * <br>
 * Not guaranteed to be globally unique, so use in combination with
 * {@link uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId
 * GoCardlessOrganisationId} to retrieve a mandate
 * 
 * @see <a href="https://developer.gocardless.com/api-reference/#core-endpoints-mandates">GoCardless Mandates</a>
 */
public class GoCardlessMandateId extends PaymentProviderMandateId {
    
    private GoCardlessMandateId(String goCardlessEventId) {
        super(goCardlessEventId);
    }

    public static GoCardlessMandateId valueOf(String goCardlessMandateId) {
        return new GoCardlessMandateId(goCardlessMandateId);
    }
}
