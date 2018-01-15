package uk.gov.pay.directdebit.common.exception.validation;

import java.util.List;

public class InvalidSizeFieldsException extends ValidationException {
    public InvalidSizeFieldsException(List<String> fields) {
        super("Field(s) are too big: [%s]", fields);
    }
}
