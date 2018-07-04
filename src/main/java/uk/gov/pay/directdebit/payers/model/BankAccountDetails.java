package uk.gov.pay.directdebit.payers.model;

public class BankAccountDetails {

    private final String accountNumber;
    private final SortCode sortCode;

    public BankAccountDetails(String accountNumber, SortCode sortCode) {
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public SortCode getSortCode() {
        return sortCode;
    }
}
