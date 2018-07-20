package uk.gov.pay.directdebit.common.exception.validation;

import java.util.List;

public class InvalidOperationException extends ValidationException {
    public InvalidOperationException(List<String> fields) {
        super("Operation(s) are invalid: [%s]", fields);
    }
}
