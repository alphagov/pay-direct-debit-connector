package uk.gov.pay.directdebit.payments.model;

/**
 * The ID assigned by GoCardless to a Direct Debit payment e.g. "PM123"
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
