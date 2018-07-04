package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang.RandomStringUtils;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class ConfirmationDetailsFixture {

    private MandateFixture mandateFixture = MandateFixture.aMandateFixture();
    private SortCode sortCode = SortCode.of(RandomStringUtils.randomNumeric(6));
    private String accountNumber = RandomStringUtils.randomNumeric(8);
    private TransactionFixture transactionFixture = null;
    
    private ConfirmationDetailsFixture() { }

    public static ConfirmationDetailsFixture confirmationDetails() {
        return new ConfirmationDetailsFixture();
    }


    public ConfirmationDetailsFixture withMandateFixture(MandateFixture mandateFixture) {
        this.mandateFixture = mandateFixture;
        return this;
    }

    public ConfirmationDetailsFixture withTransactionFixture(TransactionFixture transactionFixture) {
        this.transactionFixture = transactionFixture;
        return this;
    }
    
    public ConfirmationDetailsFixture withSortCode(SortCode sortCode) {
        this.sortCode = sortCode;
        return this;
    }

    public ConfirmationDetailsFixture withAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public ConfirmationDetails build() {
        Transaction transaction = transactionFixture != null ? transactionFixture.toEntity() : null;
        return new ConfirmationDetails(mandateFixture.toEntity(), transaction, accountNumber, sortCode);
    }

}
