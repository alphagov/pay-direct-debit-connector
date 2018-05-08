package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.BadRequestException;

import static java.lang.String.format;

public class InvalidDateException extends BadRequestException {
    public InvalidDateException(String toDate, String fromDate) {
        super(format("from_date (%s) must be earlier then to_date (%s)", toDate, fromDate));
    }
}
