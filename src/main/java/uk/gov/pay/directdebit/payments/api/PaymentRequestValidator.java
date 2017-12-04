package uk.gov.pay.directdebit.payments.api;

import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.common.validation.ApiValidation;

import java.util.Map;
import java.util.function.Function;

public class PaymentRequestValidator extends ApiValidation {
    private final static String AMOUNT_KEY = "amount";
    private final static String DESCRIPTION_KEY = "description";
    private final static String RETURN_URL_KEY = "return_url";
    private final static String REFERENCE_KEY = "reference";

    private final static int MIN_AMOUNT = 1;
    private final static int MAX_AMOUNT = 10000000;

    private final static Map<String, Function<String, Boolean>> validators = ImmutableMap.of(
            AMOUNT_KEY, (amount) -> {
                Integer amountValue = Integer.valueOf(amount);
                return MIN_AMOUNT <= amountValue && MAX_AMOUNT >= amountValue;
            }
    );
    private final static String[] requiredFields = {AMOUNT_KEY, DESCRIPTION_KEY, REFERENCE_KEY, RETURN_URL_KEY};
    private final static Map<String, Integer> maximumFieldsSize = ImmutableMap.of(
            DESCRIPTION_KEY, 255,
            REFERENCE_KEY, 255
    );

    public PaymentRequestValidator() {
        super(requiredFields, maximumFieldsSize, validators);
    }
}
