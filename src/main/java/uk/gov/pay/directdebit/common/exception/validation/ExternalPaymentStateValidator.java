package uk.gov.pay.directdebit.common.exception.validation;

import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ExternalPaymentStateValidator implements ConstraintValidator<ValidExternalPaymentState, String> {

    private Set<String> validExternalPaymentStateValues;

    @Override
    public void initialize(ValidExternalPaymentState constraintAnnotation) {
        validExternalPaymentStateValues = Arrays.stream(ExternalPaymentState.values())
                .map(ExternalPaymentState::getStatus)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }

        return validExternalPaymentStateValues.contains(value);
    }
}
