package uk.gov.pay.directdebit.mandate.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class MandateConfirmationRequest {
    @JsonProperty("sort_code")
    private String sortCode;
    
    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("transaction_external_id")
    private String transactionExternalId;

    private MandateConfirmationRequest(String sortCode, String accountNumber, String transactionExternalId) {
        this.sortCode = sortCode;
        this.accountNumber = accountNumber;
        this.transactionExternalId = transactionExternalId;
    }
    
    public static MandateConfirmationRequest of(Map<String, String> mandateConfirmation) {
        return new MandateConfirmationRequest(
                mandateConfirmation.get("sort_code"),
                mandateConfirmation.get("account_number"),
                mandateConfirmation.get("transaction_external_id")
        );
    }

    public String getSortCode() {
        return sortCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getTransactionExternalId() {
        return transactionExternalId;
    }
}
