package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class ConfirmationDetails {
    private Mandate mandate;
    private Transaction transaction;
    private AccountNumber accountNumber;
    private SortCode sortCode;

    public ConfirmationDetails(Mandate mandate,
            Transaction transaction, AccountNumber accountNumber,
            SortCode sortCode) {
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

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public SortCode getSortCode() {
        return sortCode;
    }
}
