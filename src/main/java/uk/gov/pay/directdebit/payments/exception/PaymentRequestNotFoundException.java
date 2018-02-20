package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class PaymentRequestNotFoundException extends NotFoundException {

    public PaymentRequestNotFoundException(String paymentRequestExternalId, String accountExternalId) {
        super(format("No payment request found with id: %s for gateway account with external id: %s", paymentRequestExternalId, accountExternalId));
    }
}
