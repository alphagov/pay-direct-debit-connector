package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidPaymentProviderException;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessCommandService;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.SandboxService;

import javax.inject.Inject;

public class PaymentProviderFactory {

    private SandboxService sandboxService;
    private GoCardlessService goCardlessService;
    private GoCardlessCommandService goCardlessCommandService;

    @Inject
    public PaymentProviderFactory(SandboxService sandboxService, GoCardlessService goCardlessService, GoCardlessCommandService goCardlessCommandService) {
        this.goCardlessService = goCardlessService;
        this.sandboxService = sandboxService;
        this.goCardlessCommandService = goCardlessCommandService;
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

    public DirectDebitPaymentProvideCommandService getCommandServiceFor(PaymentProvider paymentProvider) {
        switch (paymentProvider) {
            case GOCARDLESS:
                return goCardlessCommandService;
            case SANDBOX:
                return sandboxService;
        }
        throw new InvalidPaymentProviderException(paymentProvider.toString());
    }
}
