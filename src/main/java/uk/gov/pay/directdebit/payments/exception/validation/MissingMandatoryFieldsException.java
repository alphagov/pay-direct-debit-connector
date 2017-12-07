package uk.gov.pay.directdebit.payments.exception.validation;

import java.util.List;

public class MissingMandatoryFieldsException extends ValidationException {
    public MissingMandatoryFieldsException(List<String> missingFields) {
        super("Field(s) missing: [%s]", missingFields);
    }
}
