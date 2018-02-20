package uk.gov.pay.directdebit.payments.model;

import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.SandboxService;

import java.util.Map;

import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;

public class PaymentProviderMapper {

    private Map<PaymentProvider, DirectDebitPaymentProvider> providerFromType;

    public PaymentProviderMapper(SandboxService sandboxService, GoCardlessService goCardlessService) {
        providerFromType =  ImmutableMap.of(
                SANDBOX, sandboxService,
                GOCARDLESS, goCardlessService);
    }


    public DirectDebitPaymentProvider getServiceFor(PaymentProvider paymentProvider) {
        return providerFromType.get(paymentProvider);
    }
}
