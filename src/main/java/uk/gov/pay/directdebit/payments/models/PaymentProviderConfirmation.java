package uk.gov.pay.directdebit.payments.models;

public interface PaymentProviderConfirmation {
    public Entity getCustomer();
    public Entity getMandate();
}
