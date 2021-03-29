package uk.gov.pay.directdebit.mandate.model;

import uk.gov.service.payments.commons.model.WrappedStringValue;

public abstract class PaymentProviderMandateId extends WrappedStringValue {

    PaymentProviderMandateId(String paymentProviderId) {
        super(paymentProviderId);
    }

}
