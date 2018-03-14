package uk.gov.pay.directdebit.payers.api;

import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;

import java.util.Map;
import java.util.function.Function;

public class CreatePayerValidator extends ApiValidation {

    public final static String NAME_KEY = "account_holder_name";
    public final static String SORTCODE_KEY = "sort_code";
    public final static String ACCOUNT_NUMBER_KEY = "account_number";
    public final static String ADDRESS_COUNTRY_KEY = "country_code";
    public final static String ADDRESS_LINE1_KEY = "address_line1";
    public final static String ADDRESS_CITY_KEY = "city";
    public final static String ADDRESS_POSTCODE_KEY = "postcode";
    public final static String EMAIL_KEY = "email";

    private final static Map<String, Function<String, Boolean>> validators =
            ImmutableMap.<String, Function<String, Boolean>>builder()
                    .put(NAME_KEY, ApiValidation::isNotNullOrEmpty)
                    .put(SORTCODE_KEY, ApiValidation::isNumeric)
                    .put(ACCOUNT_NUMBER_KEY, ApiValidation::isNumeric)
                    .put(ADDRESS_COUNTRY_KEY, ApiValidation::isNotNullOrEmpty)
                    .put(ADDRESS_LINE1_KEY, ApiValidation::isNotNullOrEmpty)
                    .put(ADDRESS_CITY_KEY, ApiValidation::isNotNullOrEmpty)
                    .put(ADDRESS_POSTCODE_KEY, ApiValidation::isNotNullOrEmpty)
                    .put(EMAIL_KEY, ApiValidation::isNotNullOrEmpty)
                    .build();

    private final static String[] requiredFields = {NAME_KEY, SORTCODE_KEY, ACCOUNT_NUMBER_KEY, EMAIL_KEY};

    private final static Map<String, FieldSize> fieldSizes =
            ImmutableMap.<String, FieldSize>builder()
                    .put(SORTCODE_KEY, new FieldSize(6, 6))
                    .put(ACCOUNT_NUMBER_KEY, new FieldSize(6, 8))
                    .put(EMAIL_KEY, new FieldSize(0, 254))
                    .build();

    public CreatePayerValidator() {
        super(requiredFields, fieldSizes, validators);
    }

}
