package uk.gov.pay.directdebit.tokens.model;

import uk.gov.pay.directdebit.mandate.model.Mandate;

public class TokenExchangeDetails {
    private final Mandate mandate;

    public TokenExchangeDetails(Mandate mandate) {
        this.mandate = mandate;
    }

    public Mandate getMandate() {
        return mandate;
    }
}

