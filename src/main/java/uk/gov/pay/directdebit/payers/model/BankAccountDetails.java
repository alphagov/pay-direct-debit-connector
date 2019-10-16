package uk.gov.pay.directdebit.payers.model;

import java.util.Map;

public class BankAccountDetails {

    private final AccountNumber accountNumber;
    private final SortCode sortCode;

    public BankAccountDetails(AccountNumber accountNumber, SortCode sortCode) {
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public SortCode getSortCode() {
        return sortCode;
    }

    public static BankAccountDetails of(Map<String, String> bankAccountDetails) {
        AccountNumber accountNumber = AccountNumber.of(bankAccountDetails.get("account_number"));
        SortCode sortCode = SortCode.of(bankAccountDetails.get("sort_code"));
        return new BankAccountDetails(accountNumber, sortCode);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BankAccountDetails that = (BankAccountDetails) o;

        if (!accountNumber.equals(that.accountNumber)) {
            return false;
        }

        return sortCode.equals(that.sortCode);
    }

    @Override
    public int hashCode() {
        int result = accountNumber.hashCode();
        result = 31 * result + sortCode.hashCode();
        return result;
    }
}
