package uk.gov.pay.directdebit.payments.api;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidSizeFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;

import java.util.Map;

public class TransactionRequestValidatorTest {

    private TransactionRequestValidator transactionRequestValidator = new TransactionRequestValidator();
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldNotThrowExceptionIfRequestIsValid() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "100",
                "description", "bla",
                "return_url", "https://blabla.test",
                "reference", "blablabla"
        );
        transactionRequestValidator.validate(request);
    }


    @Test
    public void shouldThrowIfMissingRequiredFields() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "100",
                "description", "bla",
                "return_url", "https://blabla.test"
                );
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [reference]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        transactionRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowIfFieldsAreOfWrongSize() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "100",
                "description", RandomStringUtils.randomAlphabetic(256),
                "return_url", "https://blabla.test",
                "reference", RandomStringUtils.randomAlphabetic(256)
        );
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [description, reference]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        transactionRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowIfFieldsAreInvalid() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "10000001",
                "description", "bla",
                "return_url", "https://blabla.test",
                "reference", "blabla"
        );
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [amount]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        transactionRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAmountFieldIsNonNumeric() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "eqweqw",
                "description", "bla",
                "return_url", "https://blabla.test",
                "reference", "blabla"
        );
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [amount]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        transactionRequestValidator.validate(request);
    }
    
}
