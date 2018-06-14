package uk.gov.pay.directdebit.tokens.model;

import uk.gov.pay.directdebit.mandate.model.Mandate;

public class TokenExchangeDetails {
    private final String transactionExternalId;
    private final Mandate mandate;

    public TokenExchangeDetails(Mandate mandate, String transactionExternalId) {
        this.mandate = mandate;
        this.transactionExternalId = transactionExternalId;
    }

    public String getTransactionExternalId() {
        return transactionExternalId;
    }

    public Mandate getMandate() {
        return mandate;
    }
}

