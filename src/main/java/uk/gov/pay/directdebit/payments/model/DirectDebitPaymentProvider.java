package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payers.model.Payer;

import java.util.Map;

public interface DirectDebitPaymentProvider {

    Payer createPayer(String paymentRequestExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest);

    void confirm(String paymentRequestExternalId, GatewayAccount gatewayAccount, Map<String, String> confirmDetailsRequest);
}
