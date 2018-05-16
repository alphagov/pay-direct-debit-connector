package uk.gov.pay.directdebit.payers.model;

public class BankAccountDetails {

    private final String accountNumber;
    private final String sortCode;

    public BankAccountDetails(String accountNumber, String sortCode) {
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSortCode() {
        return sortCode;
    }
}
