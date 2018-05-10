package uk.gov.pay.directdebit.common.exception.validation;

import com.google.common.base.Joiner;
import uk.gov.pay.directdebit.common.exception.BadRequestException;

import java.util.List;

import static java.lang.String.format;

public class ValidationException extends BadRequestException {
    private static final Joiner COMMA_JOINER = Joiner.on(", ");
    ValidationException(String message, List<String> fields) {
        super(format(message, COMMA_JOINER.join(fields)));
    }
}
