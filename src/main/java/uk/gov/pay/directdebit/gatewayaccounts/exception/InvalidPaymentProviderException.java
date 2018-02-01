package uk.gov.pay.directdebit.gatewayaccounts.exception;

import uk.gov.pay.directdebit.common.exception.BadRequestException;

import static java.lang.String.format;

public class InvalidPaymentProviderException extends BadRequestException {

    public InvalidPaymentProviderException(String paymentProvider) {
        super(format("Unsupported payment provider: %s", paymentProvider));
    }
}
