package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidPaymentProviderException;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.SandboxService;

public class PaymentProviderFactory {

    private SandboxService sandboxService;
    private GoCardlessService goCardlessService;

    public PaymentProviderFactory(SandboxService sandboxService, GoCardlessService goCardlessService) {
        this.goCardlessService = goCardlessService;
        this.sandboxService = sandboxService;
    }


    public DirectDebitPaymentProvider getServiceFor(PaymentProvider paymentProvider) {
        switch (paymentProvider) {
            case GOCARDLESS:
                return goCardlessService;
            case SANDBOX:
                return sandboxService;
        }
        throw new InvalidPaymentProviderException(paymentProvider.toString());
    }
}
