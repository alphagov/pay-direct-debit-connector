package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.payments.model.Transaction;

public class ConfirmationDetails {
    private Transaction transaction;
    private Mandate mandate;

    public ConfirmationDetails(Transaction transaction, Mandate mandate) {
        this.transaction = transaction;
        this.mandate = mandate;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Mandate getMandate() {
        return mandate;
    }

    public void setMandate(Mandate mandate) {
        this.mandate = mandate;
    }
}
