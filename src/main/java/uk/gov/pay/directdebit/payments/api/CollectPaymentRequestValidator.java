package uk.gov.pay.directdebit.payments.api;

import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.common.validation.ApiValidation;
import uk.gov.pay.directdebit.common.validation.FieldSize;

import java.util.Map;
import java.util.function.Function;

public class CollectPaymentRequestValidator extends ApiValidation {

    private final static String AMOUNT_KEY = "amount";
    private final static String DESCRIPTION_KEY = "description";
    private final static String REFERENCE_KEY = "reference";
    private final static String AGREEMENT_ID_KEY = "agreement_id";

    private final static int MIN_AMOUNT_IN_PENCE = 1;
    private final static int MAX_AMOUNT_IN_PENCE = 5000_00;

    private final static Map<String, Function<String, Boolean>> validators =
            ImmutableMap.<String, Function<String, Boolean>>builder()
                    .put(AMOUNT_KEY, (amount) -> {
                        if (!isNumeric(amount)) {
                            return false;
                        }
                        Integer amountValue = Integer.valueOf(amount);
                        return MIN_AMOUNT_IN_PENCE <= amountValue && MAX_AMOUNT_IN_PENCE >= amountValue;
                    })
                    .put(DESCRIPTION_KEY, ApiValidation::isNotNullOrEmpty)
                    .put(REFERENCE_KEY, ApiValidation::isNotNullOrEmpty)
                    // TODO: re-add validation when field rename to mandate_id completed. Disabled for backwards compatibility in the meantime
//                    .put(AGREEMENT_ID_KEY, ApiValidation::isNotNullOrEmpty)
                    .build();

    private final static String[] requiredFields = {AMOUNT_KEY, DESCRIPTION_KEY, REFERENCE_KEY, AGREEMENT_ID_KEY};

    private final static Map<String, FieldSize> fieldSizes =
            ImmutableMap.<String, FieldSize>builder()
                    .put(DESCRIPTION_KEY, new FieldSize(0, 255))
                    .put(REFERENCE_KEY, new FieldSize(0, 255))
                    .put(AGREEMENT_ID_KEY, new FieldSize(0, 26))
                    .build();

    public CollectPaymentRequestValidator() {
        super(requiredFields, fieldSizes, validators);
    }

}
