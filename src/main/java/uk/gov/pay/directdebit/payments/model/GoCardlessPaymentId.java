package uk.gov.pay.directdebit.payments.model;

/**
 * The ID assigned by GoCardless to a Direct Debit payment e.g. "PM123"
 * <br>
 * Not guaranteed to be globally unique, so use in combination with
 * {@link uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId
 * GoCardlessOrganisationId} to retrieve a payment
 * 
 * @see <a href="https://developer.gocardless.com/api-reference/#core-endpoints-payments">GoCardless Payments</a>
 */
public class GoCardlessPaymentId extends PaymentProviderPaymentId {

    private GoCardlessPaymentId(String goCardlessPaymentId) {
        super(goCardlessPaymentId);
    }

    public static GoCardlessPaymentId valueOf(String goCardlessPaymentId) {
        return new GoCardlessPaymentId(goCardlessPaymentId);
    }
}
