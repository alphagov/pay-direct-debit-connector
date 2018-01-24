package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;

import java.util.Map;
import java.util.function.Function;

public class GatewayAccountValidator extends ApiValidation {

    private static final String PAYMENT_PROVIDER_KEY = "payment_provider";
    private static final String SERVICE_NAME_KEY = "service_name";

    private static final int SERVICE_NAME_FIELD_LENGTH = 50;

    private final static String[] requiredFields = {PAYMENT_PROVIDER_KEY, SERVICE_NAME_KEY};

    private final static Map<String, FieldSize> fieldSizes =
            ImmutableMap.<String, FieldSize>builder()
                    .put(SERVICE_NAME_KEY, new FieldSize(0, SERVICE_NAME_FIELD_LENGTH))
                    .build();

    public GatewayAccountValidator() {
        super(requiredFields, fieldSizes, validators);
    }

}
