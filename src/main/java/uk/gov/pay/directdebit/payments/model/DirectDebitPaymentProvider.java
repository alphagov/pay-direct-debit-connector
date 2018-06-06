package uk.gov.pay.directdebit.payments.model;

import java.util.Map;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.Payer;

public interface DirectDebitPaymentProvider {

    Payer createPayer(String mandateExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest);
    
    void confirm(String mandateExternalId, GatewayAccount gatewayAccount, Map<String, String> confirmDetailsRequest);

    BankAccountValidationResponse validate(String mandateExternalId, Map<String,String> bankAccountDetails);
    
}
