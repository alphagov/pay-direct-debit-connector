package uk.gov.pay.directdebit.gatewayaccounts.exception;

import uk.gov.pay.directdebit.common.exception.BadRequestException;

import static java.lang.String.format;

public class InvalidGatewayAccountTypeException extends BadRequestException {

    public InvalidGatewayAccountTypeException(String type) {
        super(format("Unsupported gateway account type: %s", type));
    }
}
