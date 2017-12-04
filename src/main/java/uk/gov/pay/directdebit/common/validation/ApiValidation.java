package uk.gov.pay.directdebit.common.validation;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public abstract class ApiValidation {
    private String[] requiredFields;
    private Map<String, Integer> maximumFieldsSize;
    private Map<String, Function<String, Boolean>> validators;

    public ApiValidation(String[] requiredFields, Map<String, Integer> maximumFieldsSize, Map<String, Function<String, Boolean>> validators) {
        this.requiredFields = requiredFields;
        this.maximumFieldsSize = maximumFieldsSize;
        this.validators = validators;
    }

    private boolean isFieldSizeValid(Map<String, String> chargeRequest, String fieldName, int fieldSize) {
        Optional<String> value = Optional.ofNullable(chargeRequest.get(fieldName));
        return !value.isPresent() || value.get().length() <= fieldSize; //already checked that mandatory fields are already there
    }

    private List<String> checkMissingFields(Map<String, String> inputData, String[] requiredFields) {
        return stream(requiredFields)
                .filter(field -> !inputData.containsKey(field))
                .collect(Collectors.toList());
    }

    private List<String> checkInvalidSizeFields(Map<String, String> inputData, Map<String, Integer> maximumFieldsSize) {
        return maximumFieldsSize.entrySet().stream()
                .filter(entry -> !isFieldSizeValid(inputData, entry.getKey(), entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> validateParams(Map<String, String> inputData, Map<String, Function<String, Boolean>> validators) {
        return inputData.entrySet().stream()
                .filter(entry -> Optional.ofNullable(validators.get(entry.getKey()))
                        .map(validator -> !validator.apply(entry.getValue()))
                        .orElse(false))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Optional<ValidationError> validate(Map<String, String>  request) {
        List<String> missingFields = checkMissingFields(request, requiredFields);
        if (!missingFields.isEmpty()) {
            return Optional.of(new ValidationError(ValidationError.ErrorType.MISSING_MANDATORY_FIELDS, missingFields));
        }
        List<String> invalidSizeFields = checkInvalidSizeFields(request, maximumFieldsSize);
        if (!invalidSizeFields.isEmpty()) {
            return Optional.of(new ValidationError(ValidationError.ErrorType.INVALID_SIZE_FIELDS, invalidSizeFields));
        }
        List<String> invalidFields = validateParams(request, validators);
        if (!invalidFields.isEmpty()) {
            return Optional.of(new ValidationError(ValidationError.ErrorType.INVALID_FIELDS, invalidFields));
        }
        return Optional.empty();
    }
}
