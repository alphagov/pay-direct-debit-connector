package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;

public class GoCardlessConfirmationDetails {
    private GoCardlessMandate goCardlessMandate;
    private GoCardlessCustomer goCardlessCustomer;
    private GoCardlessPayment goCardlessPayment;

    public GoCardlessConfirmationDetails(GoCardlessMandate goCardlessMandate, GoCardlessCustomer goCardlessCustomer, GoCardlessPayment goCardlessPayment) {
        this.goCardlessMandate = goCardlessMandate;
        this.goCardlessCustomer = goCardlessCustomer;
        this.goCardlessPayment = goCardlessPayment;
    }

    public GoCardlessConfirmationDetails(GoCardlessMandate goCardlessMandate, GoCardlessCustomer goCardlessCustomer) {
        this.goCardlessMandate = goCardlessMandate;
        this.goCardlessCustomer = goCardlessCustomer;
    }

    public GoCardlessMandate getMandate() {
        return goCardlessMandate;
    }

    public GoCardlessCustomer getCustomer() {
        return goCardlessCustomer;
    }

    public GoCardlessPayment getPayment() {
        return goCardlessPayment;
    }
}
