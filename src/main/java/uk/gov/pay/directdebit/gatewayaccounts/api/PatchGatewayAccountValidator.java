package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.google.common.collect.ImmutableMap;
import jersey.repackaged.com.google.common.collect.ImmutableList;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidOperationException;
import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;

public class PatchGatewayAccountValidator extends ApiValidation {

    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String ORGANISATION_KEY = "organisation";
    private static final String OPERATION_KEY = "op";
    private static final String PATH_KEY = "path";
    private static final String VALUE_KEY = "value";
    private static final String REPLACE_OPERATION = "replace";

    private static final int VALUE_FIELD_LENGTH = 255;

    private final static String[] requiredFields = {OPERATION_KEY, PATH_KEY, VALUE_KEY};
    private static final List<String> PATCH_ALLOWED_PATHS = ImmutableList.of(ACCESS_TOKEN_KEY, ORGANISATION_KEY);
    private static final List<String> PATCH_ALLOWED_OPERATION = ImmutableList.of(REPLACE_OPERATION);
    private final static Map<String, FieldSize> fieldSizes =
            ImmutableMap.<String, FieldSize>builder()
                    .put(VALUE_KEY, new FieldSize(1, VALUE_FIELD_LENGTH))
                    .build();

    private final static Map<String, Function<String, Boolean>> validators =
            ImmutableMap.<String, Function<String, Boolean>>builder()
                    .put(OPERATION_KEY, ApiValidation::isNotNullOrEmpty)
                    .put(PATH_KEY, ApiValidation::isNotNullOrEmpty)
                    .put(VALUE_KEY, ApiValidation::isNotNullOrEmpty)
                    .build();

    public PatchGatewayAccountValidator() {
        super(requiredFields, fieldSizes, validators);
    }

    public void validatePatchRequest(String externalId, List<Map<String, String>> request) {
        if (request == null) {
            throw new BadRequestException("Patch payload contains no operations");
        }
        if (request.size() != 2) {
            throw new BadRequestException(format("Patch payload contains wrong size of operations (%s)", request.size()));
        }
        for (int i = 0; i < request.size(); i++) {
            super.validate(externalId, request.get(i));
            isPathAllowed(request.get(i).get(PATH_KEY));
            isOperationAllowed(request.get(i).get(OPERATION_KEY));
        }
    }
    
    private void isPathAllowed(String path) {
        if (!PATCH_ALLOWED_PATHS.contains(path)) {
            throw new InvalidFieldsException(ImmutableList.of(path));
        }
    }
    
    private void isOperationAllowed(String operation) {
        if (!PATCH_ALLOWED_OPERATION.contains(operation)) {
            throw new InvalidOperationException(ImmutableList.of(operation));
        }
    }
}
