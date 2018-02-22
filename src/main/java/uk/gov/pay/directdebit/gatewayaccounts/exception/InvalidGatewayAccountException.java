package uk.gov.pay.directdebit.gatewayaccounts.exception;


import uk.gov.pay.directdebit.common.exception.BadRequestException;

import static java.lang.String.format;

public class InvalidGatewayAccountException extends BadRequestException {

    public InvalidGatewayAccountException(String type) {
        super(format("Unsupported gateway account: %s", type));
    }
}
