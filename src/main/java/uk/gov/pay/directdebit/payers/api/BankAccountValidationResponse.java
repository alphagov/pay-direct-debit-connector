package uk.gov.pay.directdebit.payers.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class BankAccountValidationResponse {

    @JsonProperty("is_valid")
    private boolean isValid;

    @JsonProperty("bank_name")
    private String bankName;


    public BankAccountValidationResponse(boolean isValid, String bankName) {
        this.isValid = isValid;
        this.bankName = bankName;
    }

    public BankAccountValidationResponse(boolean isValid) {
        this(isValid, null);
    }

    public boolean isValid() {
        return isValid;
    }

    public String getBankName() {
        return bankName;
    }
}
