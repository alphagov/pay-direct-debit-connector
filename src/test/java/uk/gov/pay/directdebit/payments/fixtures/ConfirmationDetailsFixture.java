package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang.RandomStringUtils;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;

public class ConfirmationDetailsFixture {

    private TransactionFixture transactionFixture;
    private MandateFixture mandateFixture;
    private String sortCode = RandomStringUtils.randomNumeric(6);
    private String accountNumber = RandomStringUtils.randomNumeric(8);

    private ConfirmationDetailsFixture() { }

    public static ConfirmationDetailsFixture confirmationDetails() {
        return new ConfirmationDetailsFixture();
    }

    public ConfirmationDetailsFixture withTransaction(TransactionFixture transaction) {
        this.transactionFixture = transaction;
        return this;
    }

    public ConfirmationDetailsFixture withMandate(MandateFixture mandateFixture) {
        this.mandateFixture = mandateFixture;
        return this;
    }

    public ConfirmationDetailsFixture withSortCode(String sortCode) {
        this.sortCode = sortCode;
        return this;
    }

    public ConfirmationDetailsFixture withAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public ConfirmationDetails build() {
        return new ConfirmationDetails(transactionFixture.toEntity(), mandateFixture.toEntity(), accountNumber, sortCode);
    }

}
