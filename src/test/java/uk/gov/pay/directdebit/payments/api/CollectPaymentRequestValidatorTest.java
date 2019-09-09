package uk.gov.pay.directdebit.payments.api;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidSizeFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;

import java.util.HashMap;
import java.util.Map;

public class CollectPaymentRequestValidatorTest {
    
    private CollectPaymentRequestValidator collectPaymentRequestValidator = new CollectPaymentRequestValidator();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldNotThrowExceptionIfRequestIsValid() {
        Map<String, String> request = generateValidRequest();
        collectPaymentRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfMissingRequiredFields() {
        Map<String, String> request = new HashMap<>();
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [amount, reference, mandate_id]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        collectPaymentRequestValidator.validate(request);
    }
    
    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfAmountFieldIsMissing() {
        Map<String, String> request = generateValidRequest();
        request.remove("amount");
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [amount]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        collectPaymentRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAmountFieldIsNonNumeric() {
        Map<String, String> request = generateValidRequest();
        request.put("amount", "one-hundred pounds");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [amount]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        collectPaymentRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAmountFieldIsZero() {
        Map<String, String> request = generateValidRequest();
        request.put("amount", "0");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [amount]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        collectPaymentRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfAmountFieldIsTooLarge() {
        Map<String, String> request = generateValidRequest();
        request.put("amount", "500001");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [amount]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        collectPaymentRequestValidator.validate(request);
    }
    
    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfDescriptionFieldHasInvalidSize() {
        Map<String, String> request = generateValidRequest();
        request.put("description", RandomStringUtils.randomAlphanumeric(256));
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [description]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        collectPaymentRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfReferenceFieldHasInvalidSize() {
        Map<String, String> request = generateValidRequest();
        request.put("reference", RandomStringUtils.randomAlphanumeric(256));
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [reference]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        collectPaymentRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfAgreementIdFieldHasInvalidSize() {
        Map<String, String> request = generateValidRequest();
        request.put("mandate_id", RandomStringUtils.randomAlphanumeric(27));
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [mandate_id]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        collectPaymentRequestValidator.validate(request);
    }

    private Map<String, String> generateValidRequest() {
        Map<String, String> request = new HashMap<>();
        request.put("amount", "10000");
        request.put("description", "A description");
        request.put("reference", "ref123");
        request.put("mandate_id", "d’accordo");
        return request;
    }

}
