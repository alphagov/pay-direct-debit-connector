package uk.gov.pay.directdebit.payers.model;

import java.util.Map;

public class BankAccountDetailsParser {
    public BankAccountDetails parse(Map<String, String> bankAccountDetails) {
            String accountNumber = bankAccountDetails.get("account_number");
            String sortCode = bankAccountDetails.get("sort_code");
            return new BankAccountDetails(accountNumber, sortCode);
        }
}
