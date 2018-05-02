package uk.gov.pay.directdebit.payers.api;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidSizeFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;

public class CreatePayerValidatorTest {

    private CreatePayerValidator createPayerValidator = new CreatePayerValidator();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldNotThrowExceptionIfRequestIsValid() {
        Map<String, String> request = generateValidRequest();
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfMissingRequiredFields() {
        Map<String, String> request = new HashMap<>();
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [" +
                CreatePayerValidator.NAME_KEY + ", " +
                CreatePayerValidator.SORT_CODE_KEY + ", " +
                CreatePayerValidator.ACCOUNT_NUMBER_KEY + ", " +
                CreatePayerValidator.EMAIL_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfNameFieldIsMissing() {
        Map<String, String> request = generateValidRequest();
        request.remove(CreatePayerValidator.NAME_KEY);
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [" +
                CreatePayerValidator.NAME_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfNameFieldIsEmptyString() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.NAME_KEY, "");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.NAME_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfNameFieldIsNull() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.NAME_KEY, null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.NAME_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfSortCodeFieldIsMissing() {
        Map<String, String> request = generateValidRequest();
        request.remove(CreatePayerValidator.SORT_CODE_KEY);
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [" +
                CreatePayerValidator.SORT_CODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfSortCodeFieldIsEmptyString() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.SORT_CODE_KEY, "");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [" +
                CreatePayerValidator.SORT_CODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfSortCodeFieldIsNull() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.SORT_CODE_KEY, null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.SORT_CODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfSortCodeFieldIsNonNumeric() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.SORT_CODE_KEY, "123abc");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.SORT_CODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfSortCodeFieldHasInvalidSize() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.SORT_CODE_KEY, "1234560");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [" +
                CreatePayerValidator.SORT_CODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldNotThrowExceptionIfSortCodeStartsWithZeros() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.SORT_CODE_KEY, "012345");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfAccountNumberFieldIsMissing() {
        Map<String, String> request = generateValidRequest();
        request.remove(CreatePayerValidator.ACCOUNT_NUMBER_KEY);
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [" +
                CreatePayerValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfAccountNumberFieldIsEmptyString() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ACCOUNT_NUMBER_KEY, "");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [" +
                CreatePayerValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAccountNumberFieldIsNull() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ACCOUNT_NUMBER_KEY, null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAccountNumberFieldIsNonNumeric() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ACCOUNT_NUMBER_KEY, "12345abc");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfAccountNumberFieldHasInvalidSizeBelowMinimum() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ACCOUNT_NUMBER_KEY, "12345");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [" +
                CreatePayerValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfAccountNumberFieldHasInvalidSizeAboveMaximum() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ACCOUNT_NUMBER_KEY, "123456780");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [" +
                CreatePayerValidator.ACCOUNT_NUMBER_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldNotThrowExceptionIfAccountNumberStartsWithZeros() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ACCOUNT_NUMBER_KEY, "01234567");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAddressCountryFieldIsEmptyString() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ADDRESS_COUNTRY_KEY, "");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.ADDRESS_COUNTRY_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAddressCountryFieldIsNull() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ADDRESS_COUNTRY_KEY, null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.ADDRESS_COUNTRY_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAddressLine1FieldIsEmptyString() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ADDRESS_LINE1_KEY, "");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.ADDRESS_LINE1_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAddressLine1FieldIsNull() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ADDRESS_LINE1_KEY, null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.ADDRESS_LINE1_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAddressCityFieldIsEmptyString() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ADDRESS_CITY_KEY, "");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.ADDRESS_CITY_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAddressCityFieldIsNull() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ADDRESS_CITY_KEY, null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.ADDRESS_CITY_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAddressPostcodeFieldIsEmptyString() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ADDRESS_POSTCODE_KEY, "");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.ADDRESS_POSTCODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAddressPostcodeFieldIsNull() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.ADDRESS_POSTCODE_KEY, null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.ADDRESS_POSTCODE_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfEmailFieldIsMissing() {
        Map<String, String> request = generateValidRequest();
        request.remove(CreatePayerValidator.EMAIL_KEY);
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [" +
                CreatePayerValidator.EMAIL_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfEmailFieldIsEmptyString() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.EMAIL_KEY, "");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.EMAIL_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfEmailFieldIsNull() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.EMAIL_KEY, null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                CreatePayerValidator.EMAIL_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createPayerValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfEmailFieldHasInvalidSizeAboveMaximum() {
        Map<String, String> request = generateValidRequest();
        request.put(CreatePayerValidator.EMAIL_KEY, RandomStringUtils.randomAlphabetic(242) + "@example.test");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [" +
                CreatePayerValidator.EMAIL_KEY +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        createPayerValidator.validate(request);
    }

    private Map<String, String> generateValidRequest() {
        PayerFixture payerFixture = aPayerFixture();
        Map<String, String> request = new HashMap<>();
        request.put(CreatePayerValidator.NAME_KEY, payerFixture.getName());
        request.put(CreatePayerValidator.SORT_CODE_KEY, payerFixture.getSortCode());
        request.put(CreatePayerValidator.ACCOUNT_NUMBER_KEY, payerFixture.getAccountNumber());
        request.put(CreatePayerValidator.EMAIL_KEY, payerFixture.getEmail());
        return request;
    }

}
