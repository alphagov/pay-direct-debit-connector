package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.payments.model.Transaction;

public class ConfirmationDetails {
    private Transaction transaction;
    private Mandate mandate;
    private String accountNumber;
    private String sortCode;

    public ConfirmationDetails(Transaction transaction,
            Mandate mandate, String accountNumber, String sortCode) {
        this.transaction = transaction;
        this.mandate = mandate;
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Mandate getMandate() {
        return mandate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSortCode() {
        return sortCode;
    }
}
