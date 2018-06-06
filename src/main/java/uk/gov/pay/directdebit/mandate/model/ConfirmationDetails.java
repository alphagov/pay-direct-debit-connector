package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.payments.model.Transaction;

public class ConfirmationDetails {
    private Mandate mandate;
    private Transaction transaction;
    private String accountNumber;
    private String sortCode;

    public ConfirmationDetails(Mandate mandate,
            Transaction transaction, String accountNumber,
            String sortCode) {
        this.mandate = mandate;
        this.transaction = transaction;
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
