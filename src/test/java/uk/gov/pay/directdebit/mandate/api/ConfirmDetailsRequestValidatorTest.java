package uk.gov.pay.directdebit.mandate.api;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidSizeFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;

public class ConfirmDetailsRequestValidatorTest {

    private ConfirmDetailsRequestValidator confirmDetailsRequestValidator = new ConfirmDetailsRequestValidator();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldNotThrowExceptionIfRequestIsValid() {
        Map<String, String> request = ImmutableMap
                .of("sort_code", "123456", "account_number", "12345678");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfMissingRequiredFields() {
        Map<String, String> request = new HashMap<>();
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [" +
                ConfirmDetailsRequestValidator.SORTCODE_KEY + ", " +
                ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfSortCodeFieldIsMissing() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, "123456");
        request.remove(ConfirmDetailsRequestValidator.SORTCODE_KEY);
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [" +
                ConfirmDetailsRequestValidator.SORTCODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfSortCodeFieldIsEmptyString() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, "123456");
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, "");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [" +
                ConfirmDetailsRequestValidator.SORTCODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfSortCodeFieldIsNull() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, "123456");
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                ConfirmDetailsRequestValidator.SORTCODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfSortCodeFieldIsNonNumeric() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, "123456");
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, "123abc");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                ConfirmDetailsRequestValidator.SORTCODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfSortCodeFieldHasInvalidSize() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, "123456");
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, "123");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [" +
                ConfirmDetailsRequestValidator.SORTCODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldNotThrowExceptionIfSortCodeStartsWithZeros() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, "123456");
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, "012345");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfAccountNumberFieldIsMissing() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, "123456");
        request.remove(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY);
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [" +
                ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfAccountNumberFieldIsEmptyString() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, "123456");
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, "");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [" +
                ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAccountNumberFieldIsNull() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, "123456");
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAccountNumberFieldIsNonNumeric() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, "123456");
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, "12345abc");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfAccountNumberFieldHasInvalidSizeBelowMinimum() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, "123456");
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, "12345");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [" +
                ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfAccountNumberFieldHasInvalidSizeAboveMaximum() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, "123456");
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, "123456780");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [" +
                ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        confirmDetailsRequestValidator.validate(request);
    }

    @Test
    public void shouldNotThrowExceptionIfAccountNumberStartsWithZeros() {
        Map<String, String> request = new HashMap<>();
        request.put(ConfirmDetailsRequestValidator.SORTCODE_KEY, "123456");
        request.put(ConfirmDetailsRequestValidator.ACCOUNT_NUMBER_KEY, "01234567");
        confirmDetailsRequestValidator.validate(request);
    }
}
