package uk.gov.pay.directdebit.gatewayaccounts.exception;

import uk.gov.pay.directdebit.common.exception.NotFoundException;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import static java.lang.String.format;

public class GatewayAccountNotFoundException extends NotFoundException {

    public GatewayAccountNotFoundException(String gatewayAccountId) {
        super(format("Unknown gateway account: %s", gatewayAccountId), ErrorIdentifier.GENERIC);
    }
}
