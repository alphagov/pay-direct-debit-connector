package uk.gov.pay.directdebit.payments.api;

import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;

import java.util.Map;
import java.util.function.Function;

public class CollectPaymentRequestValidator extends ApiValidation {

    private final static String AMOUNT_KEY = "amount";
    private final static String DESCRIPTION_KEY = "description";
    private final static String REFERENCE_KEY = "reference";
    private final static String MANDATE_ID_KEY = "mandate_id";

    private final static int MIN_AMOUNT_IN_PENCE = 1;
    private final static int MAX_AMOUNT_IN_PENCE = 5000_00;

    private final static Map<String, Function<String, Boolean>> validators =
            Map.of(
                    AMOUNT_KEY, (amount) -> {
                        if (!isNumeric(amount)) {
                            return false;
                        }
                        Integer amountValue = Integer.valueOf(amount);
                        return MIN_AMOUNT_IN_PENCE <= amountValue && MAX_AMOUNT_IN_PENCE >= amountValue;
                    },
                    DESCRIPTION_KEY, ApiValidation::isNotNullOrEmpty,
                    REFERENCE_KEY, ApiValidation::isNotNullOrEmpty,
                    MANDATE_ID_KEY, ApiValidation::isNotNullOrEmpty);

    private final static String[] requiredFields = {
            AMOUNT_KEY,
            DESCRIPTION_KEY,
            REFERENCE_KEY,
            MANDATE_ID_KEY
    };

    private final static Map<String, FieldSize> fieldSizes =
            Map.of(
                    DESCRIPTION_KEY, new FieldSize(0, 255),
                    REFERENCE_KEY, new FieldSize(0, 255),
                    MANDATE_ID_KEY, new FieldSize(0, 26));

    public CollectPaymentRequestValidator() {
        super(requiredFields, fieldSizes, validators);
    }

}
