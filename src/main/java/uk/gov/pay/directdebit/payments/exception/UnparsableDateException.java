package uk.gov.pay.directdebit.payments.exception;

import uk.gov.pay.directdebit.common.exception.BadRequestException;

import static java.lang.String.format;

public class UnparsableDateException extends BadRequestException  {
    public UnparsableDateException(String fieldName, String value) {
        super(format("Input %s (%s) is wrong format", fieldName, value));
    }
}
