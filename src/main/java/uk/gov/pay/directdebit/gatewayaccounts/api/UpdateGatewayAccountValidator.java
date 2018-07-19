package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;

import java.util.Map;
import java.util.function.Function;

public class UpdateGatewayAccountValidator extends ApiValidation {

    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String ORGANISATION_KEY = "organisation";

    private static final int VALUE_FIELD_LENGTH = 255;

    private final static String[] requiredFields = {ACCESS_TOKEN_KEY, ORGANISATION_KEY};

    private final static Map<String, FieldSize> fieldSizes =
            ImmutableMap.<String, FieldSize>builder()
                    .put(ACCESS_TOKEN_KEY, new FieldSize(1, VALUE_FIELD_LENGTH))
                    .put(ORGANISATION_KEY, new FieldSize(1, VALUE_FIELD_LENGTH))
                    .build();

    private final static Map<String, Function<String, Boolean>> validators =
            ImmutableMap.<String, Function<String, Boolean>>builder()
                    .put(ORGANISATION_KEY, ApiValidation::isNotNullOrEmpty)
                    .put(ACCESS_TOKEN_KEY, ApiValidation::isNotNullOrEmpty)
                    .build();

    public UpdateGatewayAccountValidator() {
        super(requiredFields, fieldSizes, validators);
    }
}
