package uk.gov.pay.directdebit.common.validation;

public enum FieldSizeValidator {
    ACCOUNT_NUMBER(new FieldSize(6, 8)),
    SORT_CODE(new FieldSize(6, 6));

    private FieldSize fieldSize;

    FieldSizeValidator(FieldSize fieldSize) {
        this.fieldSize = fieldSize;
    }

    public FieldSize getFieldSize() {
        return fieldSize;
    }
}
