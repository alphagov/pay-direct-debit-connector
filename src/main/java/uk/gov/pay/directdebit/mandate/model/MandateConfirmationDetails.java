package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.SortCode;

public class MandateConfirmationDetails {
    private final Mandate mandate;
    private final BankAccountDetails bankAccountDetails;

    public static MandateConfirmationDetails from(Mandate mandate, MandateConfirmationRequest confirmDetailsRequest) {
        return new MandateConfirmationDetails(
                mandate,
                new BankAccountDetails(
                        AccountNumber.of(confirmDetailsRequest.getAccountNumber()),
                        SortCode.of(confirmDetailsRequest.getSortCode()))
        );
    }

    private MandateConfirmationDetails(Mandate mandate, BankAccountDetails bankAccountDetails) {
        this.mandate = mandate;
        this.bankAccountDetails = bankAccountDetails;
    }

    public Mandate getMandate() {
        return mandate;
    }

    public BankAccountDetails getBankAccountDetails() {
        return bankAccountDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MandateConfirmationDetails that = (MandateConfirmationDetails) o;

        if (!mandate.equals(that.mandate)) return false;
        return bankAccountDetails.equals(that.bankAccountDetails);
    }

    @Override
    public int hashCode() {
        int result = mandate.hashCode();
        result = 31 * result + bankAccountDetails.hashCode();
        return result;
    }
}
