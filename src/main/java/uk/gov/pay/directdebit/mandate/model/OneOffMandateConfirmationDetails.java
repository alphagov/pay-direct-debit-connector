package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class OneOffMandateConfirmationDetails {
    private Mandate mandate;
    private BankAccountDetails bankAccountDetails;
    private Transaction transaction;
    
    public static OneOffMandateConfirmationDetails from(Mandate mandate, Transaction transaction, MandateConfirmationRequest mandateConfirmationRequest) {
        return new OneOffMandateConfirmationDetails(
                mandate,
                transaction,
                new BankAccountDetails(
                        AccountNumber.of(mandateConfirmationRequest.getAccountNumber()), 
                        SortCode.of(mandateConfirmationRequest.getSortCode()))
        );
    }

    private OneOffMandateConfirmationDetails(
            Mandate mandate,
            Transaction transaction,
            BankAccountDetails bankAccountDetails
            ) { 
        this.mandate = mandate;
        this.bankAccountDetails = bankAccountDetails;
        this.transaction = transaction;
    }

    public Mandate getMandate() {
        return mandate;
    }

    public BankAccountDetails getBankAccountDetails() {
        return bankAccountDetails;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}

