package uk.gov.pay.directdebit.gatewayaccounts.exception;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;

import static java.lang.String.format;

public class GatewayAccountMissingOrganisationIdException extends RuntimeException {
    public GatewayAccountMissingOrganisationIdException(GatewayAccount gatewayAccount) {
        super(format("Non-sandbox gateway account %s is missing an organisation id", gatewayAccount.getExternalId()));
    }
}
