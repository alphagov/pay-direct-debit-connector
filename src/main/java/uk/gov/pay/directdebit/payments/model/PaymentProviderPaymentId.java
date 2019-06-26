package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.commons.model.WrappedStringValue;

public abstract class PaymentProviderPaymentId extends WrappedStringValue {

    PaymentProviderPaymentId(String paymentProviderId) {
        super(paymentProviderId);
    }

}
