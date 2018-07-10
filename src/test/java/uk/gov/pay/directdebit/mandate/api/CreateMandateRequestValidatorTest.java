package uk.gov.pay.directdebit.mandate.api;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidSizeFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;

public class CreateMandateRequestValidatorTest {

        private CreateMandateRequestValidator createMandateRequestValidator = new CreateMandateRequestValidator();

        @Rule
        public ExpectedException thrown = ExpectedException.none();

        @Test
        public void shouldNotThrowExceptionIfRequestIsValid() {
            Map<String, String> request = ImmutableMap.of(
                    "agreement_type", "ON_DEMAND",
                    "return_url", "https://blabla.test",
                    "service_reference", "blablabla"
            );
            createMandateRequestValidator.validate(request);
        }
        
        @Test
        public void shouldThrowIfMissingRequiredFields() {
            Map<String, String> request = ImmutableMap.of(
                    "agreement_type", "ON_DEMAND",
                    "service_reference", "blablabla"
            );
            thrown.expect(MissingMandatoryFieldsException.class);
            thrown.expectMessage("Field(s) missing: [return_url]");
            thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
            createMandateRequestValidator.validate(request);
        }
        
        @Test
        public void shouldThrowIfFieldsAreInvalid() {
            Map<String, String> request = ImmutableMap.of(
                    "agreement_type", "",
                    "return_url", "blabla.test",
                    "service_reference", "blablabla"
            );
            thrown.expect(InvalidFieldsException.class);
            thrown.expectMessage("Field(s) are invalid: [agreement_type]");
            thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
            createMandateRequestValidator.validate(request);
        }

    @Test
    public void shouldThrowIfFieldsHaveTheWrongSize() {
        Map<String, String> request = ImmutableMap.of(
                "agreement_type", "ONE_OFF",
                "return_url", "https://blabla.test",
                "service_reference", RandomStringUtils.randomAlphanumeric(256)
        );
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [service_reference]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        createMandateRequestValidator.validate(request);
    }
    }
