package uk.gov.pay.directdebit.payments.exception.validation;

import java.util.List;

public class InvalidFieldsException extends ValidationException {
    public InvalidFieldsException(List<String> invalidFields) {
        super("Field(s) are invalid: [%s]", invalidFields);
    }
}
