package uk.gov.pay.directdebit.mandate.api;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;

public class CreateMandateRequestValidator extends ApiValidation {

    private final static String RETURN_URL_KEY = "return_url";
    private final static String REFERENCE_KEY = "service_reference";

    private final static Map<String, Function<String, Boolean>> validators =
            ImmutableMap.<String, Function<String, Boolean>>builder()
                    .put(RETURN_URL_KEY, ApiValidation::isNotNullOrEmpty)
                    .put(REFERENCE_KEY, ApiValidation::isNotNullOrEmpty)
                    .build();

    private final static String[] requiredFields = {RETURN_URL_KEY, REFERENCE_KEY};

    private final static Map<String, FieldSize> fieldSizes =
            ImmutableMap.<String, FieldSize>builder()
                    .put(REFERENCE_KEY, new FieldSize(0, 255))
                    .build();

    public CreateMandateRequestValidator() {
        super(requiredFields, fieldSizes, validators);
    }
}
