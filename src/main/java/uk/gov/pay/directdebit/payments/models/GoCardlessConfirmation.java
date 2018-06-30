package uk.gov.pay.directdebit.payments.models;

import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;

public final  class GoCardlessConfirmation {
    private final GoCardlessMandate goCardlessMandate;
    private final GoCardlessCustomer goCardlessCustomer;
    private final GoCardlessPayment goCardlessPayment;

    public static GoCardlessConfirmation from(GoCardlessMandate goCardlessMandate, GoCardlessCustomer goCardlessCustomer) {
        return new GoCardlessConfirmation(goCardlessMandate, goCardlessCustomer, null);
    }

    public static GoCardlessConfirmation from(
            GoCardlessMandate goCardlessMandate,
            GoCardlessCustomer goCardlessCustomer,
            GoCardlessPayment goCardlessPayment) {
        return new GoCardlessConfirmation(goCardlessMandate, goCardlessCustomer, goCardlessPayment);
    }
    
    private GoCardlessConfirmation (
            GoCardlessMandate goCardlessMandate,
            GoCardlessCustomer goCardlessCustomer,
            GoCardlessPayment goCardlessPayment) {
        this.goCardlessMandate = goCardlessMandate;
        this.goCardlessCustomer = goCardlessCustomer;
        this.goCardlessPayment = goCardlessPayment;
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
