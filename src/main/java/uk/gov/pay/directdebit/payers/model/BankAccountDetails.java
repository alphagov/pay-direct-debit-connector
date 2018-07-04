package uk.gov.pay.directdebit.payers.model;

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
}
