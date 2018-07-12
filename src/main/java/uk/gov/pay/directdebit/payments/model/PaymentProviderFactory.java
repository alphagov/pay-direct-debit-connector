package uk.gov.pay.directdebit.payments.model;

import javax.inject.Inject;
import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidPaymentProviderException;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.SandboxService;

public class PaymentProviderFactory {

    private SandboxService sandboxService;
    private GoCardlessService goCardlessService;

    @Inject
    public PaymentProviderFactory(SandboxService sandboxService, GoCardlessService goCardlessCommandService) {
        this.sandboxService = sandboxService;
        this.goCardlessService = goCardlessCommandService;
    }

    public DirectDebitPaymentProviderCommandService getCommandServiceFor(PaymentProvider paymentProvider) {
        switch (paymentProvider) {
            case GOCARDLESS:
                return goCardlessService;
            case SANDBOX:
                return sandboxService;
        }
        throw new InvalidPaymentProviderException(paymentProvider.toString());
    }
}
