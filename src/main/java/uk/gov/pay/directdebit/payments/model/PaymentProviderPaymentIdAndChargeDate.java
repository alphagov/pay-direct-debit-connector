package uk.gov.pay.directdebit.payments.model;

import java.time.LocalDate;

public class PaymentProviderPaymentIdAndChargeDate {

    private final PaymentProviderPaymentId paymentProviderPaymentId;
    private final LocalDate chargeDate;

    public PaymentProviderPaymentIdAndChargeDate(PaymentProviderPaymentId paymentProviderPaymentId, LocalDate chargeDate) {
        this.paymentProviderPaymentId = paymentProviderPaymentId;
        this.chargeDate = chargeDate;
    }

    public PaymentProviderPaymentId getPaymentProviderPaymentId() {
        return paymentProviderPaymentId;
    }

    public LocalDate getChargeDate() {
        return chargeDate;
    }

}
