package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;

import java.util.Map;

public interface DirectDebitPaymentProvider {
    void confirm(ConfirmationDetails confirmationDetails);

    Transaction collect(GatewayAccount gatewayAccount, Map<String, String> collectPaymentRequest);
    
    BankAccountValidationResponse validate(String mandateExternalId, Map<String,String> bankAccountDetails);
}
