package uk.gov.pay.directdebit.common.exception.validation;

import uk.gov.pay.directdebit.mandate.api.ExternalMandateState;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ExternalMandateStateValidator implements ConstraintValidator<ValidExternalMandateState, String> {

    private Set<String> validExternalMandateStateValues;

    @Override
    public void initialize(ValidExternalMandateState annotation) {
        validExternalMandateStateValues = Arrays.stream(ExternalMandateState.values())
                .map(ExternalMandateState::getState)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return validExternalMandateStateValues.contains(value);
    }
}
