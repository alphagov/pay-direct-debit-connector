package uk.gov.pay.directdebit.partnerapp.api;

import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static uk.gov.pay.directdebit.partnerapp.resources.GoCardlessAppConnectAccountTokenResource.CODE_FIELD;
import static uk.gov.pay.directdebit.partnerapp.resources.GoCardlessAppConnectAccountTokenResource.STATE_TOKEN_FIELD;

public class GoCardlessAppConnectAccountTokenRequestValidator extends ApiValidation {
    private static final String[] requiredFields = new String[]{CODE_FIELD, STATE_TOKEN_FIELD};
    private static final Map<String, Function<String, Boolean>> validators = new LinkedHashMap<>();
    private static final Map<String, FieldSize> fieldSizes = new LinkedHashMap<>();

    static {
        validators.put(CODE_FIELD, ApiValidation::isNotNullOrEmpty);
        validators.put(STATE_TOKEN_FIELD, ApiValidation::isNotNullOrEmpty);
        
        fieldSizes.put(CODE_FIELD, new FieldSize(1, 255));
        fieldSizes.put(STATE_TOKEN_FIELD, new FieldSize(26, 26));
    }

    private GoCardlessAppConnectAccountTokenRequestValidator() {
        super(requiredFields, fieldSizes, validators);
    }
    
    public static void validateRequest(Map<String, String> request) {
        GoCardlessAppConnectAccountTokenRequestValidator validator = new GoCardlessAppConnectAccountTokenRequestValidator();
        validator.validate(request);
    }
}
