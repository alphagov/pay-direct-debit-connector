package uk.gov.pay.directdebit.gatewayaccounts.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;

import static java.lang.String.format;

public class GatewayAccountNotFoundException extends NotFoundException {

    public GatewayAccountNotFoundException(Long gatewayAccountId) {
        super(format("Unknown gateway account: %s", gatewayAccountId));
    }
}
