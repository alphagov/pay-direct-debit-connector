package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class ConfirmationDetails {
    private Mandate mandate;
    private Transaction transaction;
    private BankAccountDetails bankAccountDetails;

    public ConfirmationDetails(Mandate mandate,
                               Transaction transaction, 
                               String accountNumber,
                               String sortCode) {
        this.mandate = mandate;
        this.transaction = transaction;
        this.bankAccountDetails = new BankAccountDetails(accountNumber, sortCode);
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Mandate getMandate() {
        return mandate;
    }

    public BankAccountDetails getBankAccountDetails() {
        return bankAccountDetails;
    }
}
