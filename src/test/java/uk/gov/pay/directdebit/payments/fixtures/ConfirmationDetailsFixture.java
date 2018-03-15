package uk.gov.pay.directdebit.payments.fixtures;

import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;

public class ConfirmationDetailsFixture {

    private TransactionFixture transactionFixture;
    private MandateFixture mandateFixture;

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

    public ConfirmationDetails build() {
        return new ConfirmationDetails(transactionFixture.toEntity(), mandateFixture.toEntity());
    }

}
