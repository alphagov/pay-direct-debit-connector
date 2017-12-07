package uk.gov.pay.directdebit.payments.api;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.payments.exception.validation.InvalidSizeFieldsException;
import uk.gov.pay.directdebit.payments.exception.validation.MissingMandatoryFieldsException;

import java.util.Map;

public class PaymentRequestValidatorTest {

    private PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldNotThrowExceptionIfRequestIsValid() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "100",
                "description", "bla",
                "return_url", "blabla",
                "reference", "blablabla"
        );
        paymentRequestValidator.validate(request);
    }


    @Test
    public void shouldThrowIfMissingRequiredFields() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "100",
                "description", "bla",
                "return_url", "blabla"
        );
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [reference]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        paymentRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowIfFieldsAreOfWrongSize() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "100",
                "description", RandomStringUtils.random(256),
                "return_url", "bla",
                "reference", RandomStringUtils.random(256)
        );
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("Field(s) are too big: [description, reference]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        paymentRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowIfFieldsAreInvalid() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "10000001",
                "description", "bla",
                "return_url", "bla",
                "reference", "blabla"
        );
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [amount]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        paymentRequestValidator.validate(request);
    }
}
