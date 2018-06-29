package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.Payer;

import java.util.Map;

public interface DirectDebitPaymentProvider {

    Payer createPayer(String mandateExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest);
    
    void confirm(String mandateExternalId, GatewayAccount gatewayAccount, Map<String, String> confirmDetailsRequest);

    Transaction collect(GatewayAccount gatewayAccount, Map<String, String> collectPaymentRequest);
    
    BankAccountValidationResponse validate(String mandateExternalId, Map<String,String> bankAccountDetails);
    
}
