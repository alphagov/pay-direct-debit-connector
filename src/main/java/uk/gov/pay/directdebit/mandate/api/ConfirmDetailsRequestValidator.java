package uk.gov.pay.directdebit.mandate.api;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;
import uk.gov.pay.directdebit.common.validation.FieldSizeValidator;

public class ConfirmDetailsRequestValidator extends ApiValidation {

    public final static String SORT_CODE_KEY = "sort_code";
    public final static String ACCOUNT_NUMBER_KEY = "account_number";

    private final static Map<String, Function<String, Boolean>> validators =
            ImmutableMap.<String, Function<String, Boolean>>builder()
                    .put(SORT_CODE_KEY, ApiValidation::isNumeric)
                    .put(ACCOUNT_NUMBER_KEY, ApiValidation::isNumeric)
                    .build();

    private final static String[] requiredFields = {SORT_CODE_KEY, ACCOUNT_NUMBER_KEY};

    private final static Map<String, FieldSize> fieldSizes =
            ImmutableMap.<String, FieldSize>builder()
                    .put(SORT_CODE_KEY, FieldSizeValidator.SORT_CODE.getFieldSize())
                    .put(ACCOUNT_NUMBER_KEY, FieldSizeValidator.ACCOUNT_NUMBER.getFieldSize())
                    .build();

    public ConfirmDetailsRequestValidator() {
        super(requiredFields, fieldSizes, validators);
    }
}
