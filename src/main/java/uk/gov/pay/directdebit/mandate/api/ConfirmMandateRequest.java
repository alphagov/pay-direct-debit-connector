package uk.gov.pay.directdebit.mandate.api;

import java.util.Map;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.SortCode;

public class ConfirmMandateRequest {
    private final SortCode sortCode;

    private final AccountNumber accountNumber;

    public ConfirmMandateRequest(SortCode sortCode, AccountNumber accountNumber) {
        this.sortCode = sortCode;
        this.accountNumber = accountNumber;
    }

    public static ConfirmMandateRequest of(Map<String, String> mandateConfirmation) {
        return new ConfirmMandateRequest(
                SortCode.of(mandateConfirmation.get("sort_code")),
                AccountNumber.of(mandateConfirmation.get("account_number"))
        );
    }

    public SortCode getSortCode() {
        return sortCode;
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }
}
