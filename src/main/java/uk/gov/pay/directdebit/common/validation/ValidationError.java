package uk.gov.pay.directdebit.common.validation;

import java.util.List;

public class ValidationError {
    private ErrorType type;
    private List<String> fields;

    ValidationError(ErrorType type, List<String> fields) {
        this.type = type;
        this.fields = fields;
    }

    public ErrorType getType() {
        return type;
    }

    public List<String> getFields() {
        return fields;
    }

    public enum ErrorType {
        MISSING_MANDATORY_FIELDS,
        INVALID_SIZE_FIELDS,
        INVALID_FIELDS
    }
}
