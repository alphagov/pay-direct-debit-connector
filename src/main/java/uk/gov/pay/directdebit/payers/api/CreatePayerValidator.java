package uk.gov.pay.directdebit.payers.api;

import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.common.validation.ApiValidation;

import java.util.Map;
import java.util.function.Function;

public class CreatePayerValidator extends ApiValidation {
    private final static String ACCOUNT_NUMBER_KEY = "account_number";
    private final static String SORTCODE_KEY = "sort_code";
    private final static String NAME_KEY = "account_holder_name";
    private final static String EMAIL_KEY = "email";
    private final static String ADDRESS_LINE1_KEY = "address_line1";
    private final static String ADDRESS_CITY_KEY = "city";
    private final static String ADDRESS_COUNTRY_KEY = "country_code";
    private final static String ADDRESS_POSTCODE_KEY = "postcode";

    private static boolean isNumeric(String string) {
        return string.matches("[0-9]+");
    }
    private final static Map<String, Function<String, Boolean>> validators = ImmutableMap.of(
            ACCOUNT_NUMBER_KEY, CreatePayerValidator::isNumeric,
            SORTCODE_KEY, CreatePayerValidator::isNumeric
    );
    private final static String[] requiredFields = {ACCOUNT_NUMBER_KEY, SORTCODE_KEY, NAME_KEY, EMAIL_KEY, ADDRESS_LINE1_KEY, ADDRESS_CITY_KEY, ADDRESS_COUNTRY_KEY, ADDRESS_POSTCODE_KEY};
    private final static Map<String, Integer> maximumFieldsSize = ImmutableMap.of(
            EMAIL_KEY, 254,
            SORTCODE_KEY, 6,
            ACCOUNT_NUMBER_KEY, 10
    );
    public CreatePayerValidator() {
        super(requiredFields, maximumFieldsSize, validators);
    }
}
