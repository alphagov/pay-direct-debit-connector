package uk.gov.pay.directdebit.common.validation;


import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.payments.exception.validation.InvalidSizeFieldsException;
import uk.gov.pay.directdebit.payments.exception.validation.MissingMandatoryFieldsException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public abstract class ApiValidation {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(ApiValidation.class);

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

    public void validate(Map<String, String>  request) {
        List<String> missingFields = checkMissingFields(request, requiredFields);
        if (!missingFields.isEmpty()) {
            LOGGER.error("Error validating request {}, missing mandatory fields", request.getOrDefault("external_id", "unknown_id"));
            throw new MissingMandatoryFieldsException(missingFields);
        }
        List<String> invalidSizeFields = checkInvalidSizeFields(request, maximumFieldsSize);
        if (!invalidSizeFields.isEmpty()) {
            LOGGER.error("Error validating request {}, fields are too big", request.getOrDefault("external_id", "unknown_id"));
            throw new InvalidSizeFieldsException(invalidSizeFields);
        }
        List<String> invalidFields = validateParams(request, validators);
        if (!invalidFields.isEmpty()) {
            LOGGER.error("Error validating request {}, fields are invalid", request.getOrDefault("external_id", "unknown_id"));
            throw new InvalidFieldsException(invalidFields);
        }
    }
}
