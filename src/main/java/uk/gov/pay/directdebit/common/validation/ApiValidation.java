package uk.gov.pay.directdebit.common.validation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidSizeFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public abstract class ApiValidation {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(ApiValidation.class);

    private String[] requiredFields;
    private Map<String, FieldSize> fieldSizes;
    private Map<String, Function<String, Boolean>> validators;

    public ApiValidation(String[] requiredFields,
                         Map<String, FieldSize> fieldSizes,
                         Map<String, Function<String, Boolean>> validators) {
        this.requiredFields = requiredFields;
        this.fieldSizes = fieldSizes;
        this.validators = validators;
    }

    public void validate(String externalId, Map<String, String> request) {
        List<String> missingFields = checkMissingFields(request, requiredFields);
        if (!missingFields.isEmpty()) {
            LOGGER.error("Error validating request {}, missing mandatory fields", externalId);
            throw new MissingMandatoryFieldsException(missingFields);
        }
        List<String> invalidSizeFields = checkFieldSizes(request, fieldSizes);
        if (!invalidSizeFields.isEmpty()) {
            LOGGER.error("Error validating request {}, the size of a field(s) is invalid", externalId);
            throw new InvalidSizeFieldsException(invalidSizeFields);
        }
        List<String> invalidFields = validateParams(request, validators);
        if (!invalidFields.isEmpty()) {
            LOGGER.error("Error validating request {}, fields are invalid", externalId);
            throw new InvalidFieldsException(invalidFields);
        }
    }

    public void validate(Map<String, String> request) {
        validate("<external id not yet created>", request);
    }

    public static boolean isNumeric(String string) {
        return (string != null) && string.matches("[0-9]+");
    }

    public static boolean isNotNullOrEmpty(String string) {
        return !StringUtils.isBlank(string);
    }

    private List<String> checkMissingFields(Map<String, String> inputData, String[] requiredFields) {
        return stream(requiredFields)
                .filter(field -> !inputData.containsKey(field))
                .collect(Collectors.toList());
    }

    private List<String> checkFieldSizes(Map<String, String> inputData, Map<String, FieldSize> fieldSizes) {
        return fieldSizes.entrySet().stream()
                .filter(entry -> !isFieldSizeValid(inputData, entry.getKey(), entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private boolean isFieldSizeValid(Map<String, String> inputData, String fieldName, FieldSize fieldSize) {
        Optional<String> value = Optional.ofNullable(inputData.get(fieldName));
        return (!value.isPresent()) || //already checked that mandatory fields are already there
                (value.get().length() >= fieldSize.getMinimum() && value.get().length() <= fieldSize.getMaximum());
    }

    private List<String> validateParams(Map<String, String> inputData, Map<String, Function<String, Boolean>> validators) {
        return inputData.entrySet().stream()
                .filter(entry -> Optional.ofNullable(validators.get(entry.getKey()))
                        .map(validator -> !validator.apply(entry.getValue()))
                        .orElse(false))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}
