package uk.gov.pay.directdebit.partnerapp.api;

import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static uk.gov.pay.directdebit.partnerapp.resources.GoCardlessAppConnectAccountStateResource.GATEWAY_ACCOUNT_ID_FIELD;
import static uk.gov.pay.directdebit.partnerapp.resources.GoCardlessAppConnectAccountStateResource.REDIRECT_URI_FIELD;

public class GoCardlessAppConnectAccountStateRequestValidator extends ApiValidation {

    private static final String[] requiredFields = new String[]{GATEWAY_ACCOUNT_ID_FIELD, REDIRECT_URI_FIELD};
    private static final Map<String, Function<String, Boolean>> validators = new LinkedHashMap<>();
    private static final Map<String, FieldSize> fieldSizes = new LinkedHashMap<>();

    static {
        validators.put(GATEWAY_ACCOUNT_ID_FIELD, ApiValidation::isNotNullOrEmpty);
        validators.put(REDIRECT_URI_FIELD, ApiValidation::isNotNullOrEmpty);

        fieldSizes.put(GATEWAY_ACCOUNT_ID_FIELD, new FieldSize(26, 26));
        fieldSizes.put(REDIRECT_URI_FIELD, new FieldSize(1, 255));
    }

    private GoCardlessAppConnectAccountStateRequestValidator() {
        super(requiredFields, fieldSizes, validators);
    }

    public static void validateRequest(Map<String, String> request) {
        GoCardlessAppConnectAccountStateRequestValidator validator = new GoCardlessAppConnectAccountStateRequestValidator();
        validator.validate(request);
    }
}
