package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidOperationException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidSizeFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;

import java.util.Arrays;
import java.util.Map;

public class PatchGatewayAccountValidatorTest {
    
    private PatchGatewayAccountValidator validator = new PatchGatewayAccountValidator();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldNotThrowExceptionIfRequestIsValid() {
        ImmutableMap<String, String> request = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "access_token")
                .put("value", RandomStringUtils.randomAlphabetic(45))
                .build();
        validator.validate(request);
    }
    
    @Test
    public void shouldThrowInvalidFieldsException_whenPathFieldIsMissing() {
        ImmutableMap<String, String> request = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("value", RandomStringUtils.randomAlphabetic(45))
                .build();
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [path]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        validator.validate(request);
    }
    
    @Test
    public void shouldThrowInvalidFieldsException_whenAccessTokenIsTooLong() {
        ImmutableMap<String, String> request = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "access_token")
                .put("value", RandomStringUtils.randomAlphabetic(256))
                .build();
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [value]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        validator.validate(request);
    }
    
    @Test
    public void shouldThrowInvalidOperationException_whenOperationIsDelete() {
        ImmutableMap<String, String> request = ImmutableMap.<String, String>builder()
                .put("op", "delete")
                .put("path", "access_token")
                .put("value", RandomStringUtils.randomAlphabetic(45))
                .build();
        thrown.expect(InvalidOperationException.class);
        thrown.expectMessage("Operation(s) are invalid: [delete]");
        thrown.reportMissingExceptionWithMessage("InvalidOperationException expected");
        validator.validatePatchRequest("an-externalId", request);
    }
    
    @Test
    public void shouldThrowInvalidFieldsException_whenValueIsMissing() {
        ImmutableMap<String, String> request = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "access_token")
                .put("value", "   ")
                .build();
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [value]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        validator.validate(request);
    }
}
