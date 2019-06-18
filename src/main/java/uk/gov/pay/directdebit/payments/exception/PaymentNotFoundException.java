package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentId;

import static java.lang.String.format;

public class PaymentNotFoundException extends NotFoundException {

    public PaymentNotFoundException(PaymentProvider provider, PaymentProviderPaymentId paymentId) {
        super(format("No payment found for provider %s with payment provider ID %s", provider, paymentId));
    }
}

