package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidPaymentProviderException;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payments.services.GoCardlessOnDemandService;
import uk.gov.pay.directdebit.payments.services.GoCardlessOneOffService;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.SandboxService;

import javax.inject.Inject;

public class PaymentProviderFactory {

    private SandboxService sandboxService;
    private GoCardlessOnDemandService goCardlessOnDemandService;
    private GoCardlessOneOffService goCardlessOneOffDemandService;

    @Inject
    public PaymentProviderFactory(SandboxService sandboxService, GoCardlessOnDemandService goCardlessOnDemandService) {
        this.goCardlessOnDemandService = goCardlessOnDemandService;
        this.sandboxService = sandboxService;
    }


    public DirectDebitPaymentProvider getServiceFor(PaymentProvider paymentProvider, MandateType mandateType) {
        switch (paymentProvider) {
            case GOCARDLESS:
                switch (mandateType) {
                    case ONE_OFF:
                        return goCardlessOneOffDemandService;
                    case ON_DEMAND:
                        return goCardlessOnDemandService;
                }
            case SANDBOX:
                return sandboxService;
        }
        throw new InvalidPaymentProviderException(paymentProvider.toString());
    }
}
