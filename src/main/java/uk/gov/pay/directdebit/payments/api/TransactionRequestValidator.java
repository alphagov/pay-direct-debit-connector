package uk.gov.pay.directdebit.payments.api;

import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;

import java.util.Map;
import java.util.function.Function;

public class TransactionRequestValidator extends ApiValidation {

    private final static String AMOUNT_KEY = "amount";
    private final static String DESCRIPTION_KEY = "description";
    private final static String RETURN_URL_KEY = "return_url";
    private final static String REFERENCE_KEY = "reference";

    private final static int MIN_AMOUNT_IN_PENCE = 1;
    private final static int MAX_AMOUNT_IN_PENCE = 5000_00;

    private final static Map<String, Function<String, Boolean>> validators =
            ImmutableMap.<String, Function<String, Boolean>>builder()
                    .put(AMOUNT_KEY, (amount) -> {
                        Integer amountValue = Integer.valueOf(amount);
                        return MIN_AMOUNT_IN_PENCE <= amountValue && MAX_AMOUNT_IN_PENCE >= amountValue;
                    })
                    .build();

    private final static String[] requiredFields = {AMOUNT_KEY, DESCRIPTION_KEY, REFERENCE_KEY, RETURN_URL_KEY};

    private final static Map<String, FieldSize> fieldSizes =
            ImmutableMap.<String, FieldSize>builder()
                    .put(DESCRIPTION_KEY, new FieldSize(0, 255))
                    .put(REFERENCE_KEY, new FieldSize(0, 255))
                    .build();

    public TransactionRequestValidator() {
        super(requiredFields, fieldSizes, validators);
    }

}
