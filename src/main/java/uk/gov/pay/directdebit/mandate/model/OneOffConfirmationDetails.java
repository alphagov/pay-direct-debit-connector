package uk.gov.pay.directdebit.mandate.model;

import java.time.LocalDate;

public class OneOffConfirmationDetails {
    private Mandate mandate;
    private LocalDate chargeDate;

    public OneOffConfirmationDetails(Mandate mandate, LocalDate chargeDate) {
        this.chargeDate = chargeDate;
        this.mandate = mandate;
    }

    public LocalDate getChargeDate() {
        return chargeDate;
    }

    public Mandate getMandate() {
        return mandate;
    }
}
