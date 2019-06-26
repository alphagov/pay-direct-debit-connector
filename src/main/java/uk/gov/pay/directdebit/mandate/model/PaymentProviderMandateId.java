package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.commons.model.WrappedStringValue;

public abstract class PaymentProviderMandateId extends WrappedStringValue {

    PaymentProviderMandateId(String paymentProviderId) {
        super(paymentProviderId);
    }

}
