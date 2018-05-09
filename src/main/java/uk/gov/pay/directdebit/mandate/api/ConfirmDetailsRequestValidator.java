package uk.gov.pay.directdebit.mandate.api;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;
import uk.gov.pay.directdebit.common.validation.FieldSizeValidator;

public class ConfirmDetailsRequestValidator extends ApiValidation {

    public final static String SORTCODE_KEY = "sort_code";
    public final static String ACCOUNT_NUMBER_KEY = "account_number";

    private final static Map<String, Function<String, Boolean>> validators =
            ImmutableMap.<String, Function<String, Boolean>>builder()
                    .put(SORTCODE_KEY, ApiValidation::isNumeric)
                    .put(ACCOUNT_NUMBER_KEY, ApiValidation::isNumeric)
                    .build();

    private final static String[] requiredFields = {SORTCODE_KEY, ACCOUNT_NUMBER_KEY};

    private final static Map<String, FieldSize> fieldSizes =
            ImmutableMap.<String, FieldSize>builder()
                    .put(SORTCODE_KEY, FieldSizeValidator.SORT_CODE.getFieldSize())
                    .put(ACCOUNT_NUMBER_KEY, FieldSizeValidator.ACCOUNT_NUMBER.getFieldSize())
                    .build();

    public ConfirmDetailsRequestValidator() {
        super(requiredFields, fieldSizes, validators);
    }
}
