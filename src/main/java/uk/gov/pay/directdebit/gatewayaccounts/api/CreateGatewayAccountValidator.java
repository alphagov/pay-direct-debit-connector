package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.common.validation.ApiValidation;

import java.util.Map;
import java.util.function.Function;

public class CreateGatewayAccountValidator extends ApiValidation {

    private static final String PAYMENT_PROVIDER_KEY = "payment_provider";
    private static final String TYPE_KEY = "type";

    private final static String[] requiredFields = {PAYMENT_PROVIDER_KEY, TYPE_KEY};
    
    private final static Map<String, Function<String, Boolean>> validators =
            ImmutableMap.<String, Function<String, Boolean>>builder()
                    .put(PAYMENT_PROVIDER_KEY, ApiValidation::isNotNullOrEmpty)
                    .put(TYPE_KEY, ApiValidation::isNotNullOrEmpty)
                    .build();

    public CreateGatewayAccountValidator() {
        super(requiredFields, Map.of(), validators);
    }

}
