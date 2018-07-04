package uk.gov.pay.directdebit.payers.model;

import java.util.Map;

public class BankAccountDetailsParser {
    public BankAccountDetails parse(Map<String, String> bankAccountDetails) {
            String accountNumber = bankAccountDetails.get("account_number");
            SortCode sortCode = SortCode.of(bankAccountDetails.get("sort_code"));
            return new BankAccountDetails(accountNumber, sortCode);
        }
}
