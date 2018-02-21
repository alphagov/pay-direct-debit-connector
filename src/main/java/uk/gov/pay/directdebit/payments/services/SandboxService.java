package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;

import java.util.Map;

public class SandboxService implements DirectDebitPaymentProvider {
    private final PayerService payerService;

    public SandboxService(PayerService payerService) {
        this.payerService = payerService;
    }

    @Override
    public Payer createPayer(String paymentRequestExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest) {
        return payerService.create(paymentRequestExternalId, gatewayAccount.getId(), createPayerRequest);
    }
}
