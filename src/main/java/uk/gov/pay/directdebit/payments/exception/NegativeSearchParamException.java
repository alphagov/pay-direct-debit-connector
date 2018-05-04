package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.BadRequestException;

import static java.lang.String.format;

public class NegativeSearchParamException extends BadRequestException {
    public NegativeSearchParamException(String paramName) {
        super(format("Query param '%s' should be a non zero positive integer", paramName));
    }
}
