package uk.gov.pay.directdebit.payers.api;

import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.common.validation.ApiValidation;

import java.util.Map;
import java.util.function.Function;

public class CreatePayerValidator extends ApiValidation {
    private final static String ACCOUNT_NUMBER_KEY = "account_number";
    private final static String SORTCODE_KEY = "sort_code";

    private static boolean isNumeric(String string) {
        return string.matches("[0-9]+");
    }
    private final static Map<String, Function<String, Boolean>> validators = ImmutableMap.of(
            ACCOUNT_NUMBER_KEY, (String accountNumber) -> accountNumber.length() == 8 && isNumeric(accountNumber),
            SORTCODE_KEY, (String sortCode) -> sortCode.length() == 6 && isNumeric(sortCode)
    );
    private final static String[] requiredFields = {ACCOUNT_NUMBER_KEY, SORTCODE_KEY};
    private final static Map<String, Integer> maximumFieldsSize = ImmutableMap.of();

    public CreatePayerValidator() {
        super(requiredFields, maximumFieldsSize, validators);
    }
}
