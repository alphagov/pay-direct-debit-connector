package uk.gov.pay.directdebit.gatewayaccounts.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidPaymentProviderException;

public enum PaymentProvider {
    SANDBOX, GOCARDLESS;

    @JsonCreator
    public static PaymentProvider fromString(String paymentProvider) {
        for (PaymentProvider typeEnum : PaymentProvider.values()) {
            if (typeEnum.toString().equalsIgnoreCase(paymentProvider)) {
                return typeEnum;
            }
        }
        throw new InvalidPaymentProviderException(paymentProvider);
    }
}
