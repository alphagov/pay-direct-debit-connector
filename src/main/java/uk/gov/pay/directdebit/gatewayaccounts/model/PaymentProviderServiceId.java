package uk.gov.pay.directdebit.gatewayaccounts.model;

import uk.gov.pay.commons.model.WrappedStringValue;

/**
 * The ID used by a payment provider to refer to a service that takes
 * payments from users
 */
public abstract class PaymentProviderServiceId extends WrappedStringValue {

    PaymentProviderServiceId(String value) {
        super(value);
    }

}
