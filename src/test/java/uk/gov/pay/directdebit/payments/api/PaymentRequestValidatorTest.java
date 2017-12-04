package uk.gov.pay.directdebit.payments.api;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import uk.gov.pay.directdebit.common.validation.ValidationError;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class PaymentRequestValidatorTest {

    private PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator();

    @Test
    public void shouldNotReturnErrorIfRequestIsValid() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "100",
                "description", "bla",
                "return_url", "blabla",
                "reference", "blablabla"
        );
        assertThat(paymentRequestValidator.validate(request).isPresent(), is(false));
    }


    @Test
    public void shouldReturnErrorIfMissingRequiredFields() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "100",
                "description", "bla",
                "return_url", "blabla"
        );
        ValidationError validationError = paymentRequestValidator.validate(request).get();
        assertThat(validationError.getType(), is(ValidationError.ErrorType.MISSING_MANDATORY_FIELDS));
        assertThat(validationError.getFields(), is(Collections.singletonList("reference")));
    }

    @Test
    public void shouldReturnErrorIfFieldsAreOfWrongSize() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "100",
                "description", RandomStringUtils.random(256),
                "return_url", "bla",
                "reference", RandomStringUtils.random(256)
        );
        ValidationError validationError = paymentRequestValidator.validate(request).get();
        assertThat(validationError.getType(), is(ValidationError.ErrorType.INVALID_SIZE_FIELDS));
        assertThat(validationError.getFields(), is(Arrays.asList("description", "reference")));
    }

    @Test
    public void shouldReturnErrorIfFieldsAreInvalid() {
        Map<String, String> request = ImmutableMap.of(
                "amount", "10000001",
                "description", "bla",
                "return_url", "bla",
                "reference", "blabla"
        );
        ValidationError validationError = paymentRequestValidator.validate(request).get();
        assertThat(validationError.getType(), is(ValidationError.ErrorType.INVALID_FIELDS));
        assertThat(validationError.getFields(), is(Collections.singletonList("amount")));
    }
}
