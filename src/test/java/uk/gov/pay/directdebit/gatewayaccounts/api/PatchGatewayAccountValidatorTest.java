package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidOperationException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidSizeFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;

import static com.google.common.collect.ImmutableList.of;

public class PatchGatewayAccountValidatorTest {
    
    private PatchGatewayAccountValidator validator = new PatchGatewayAccountValidator();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldNotThrowExceptionIfRequestIsValid() {
        ImmutableMap<String, String> request1 = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "access_token")
                .put("value", RandomStringUtils.randomAlphabetic(45))
                .build();
        ImmutableMap<String, String> request2 = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "organisation")
                .put("value", RandomStringUtils.randomAlphabetic(45))
                .build();
        validator.validatePatchRequest("an-externalId", of(request1, request2));
    }
    
    @Test
    public void shouldThrowInvalidFieldsException_whenPathFieldIsMissing() {
        ImmutableMap<String, String> request1 = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("value", RandomStringUtils.randomAlphabetic(45))
                .build();
        ImmutableMap<String, String> request2 = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "organisation")
                .put("value", RandomStringUtils.randomAlphabetic(45))
                .build();
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [path]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        validator.validatePatchRequest("an-externalId", of(request1, request2));
    }
    
    @Test
    public void shouldThrowInvalidFieldsException_whenAccessTokenIsTooLong() {
        ImmutableMap<String, String> request1 = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "access_token")
                .put("value", RandomStringUtils.randomAlphabetic(45))
                .build();
        ImmutableMap<String, String> request2 = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "organisation")
                .put("value", RandomStringUtils.randomAlphabetic(256))
                .build();
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [value]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        validator.validatePatchRequest("an-externalId",of(request1, request2));
    }
    
    @Test
    public void shouldThrowInvalidOperationException_whenOperationIsDelete() {
        ImmutableMap<String, String> request1 = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "access_token")
                .put("value", RandomStringUtils.randomAlphabetic(45))
                .build();
        ImmutableMap<String, String> request2 = ImmutableMap.<String, String>builder()
                .put("op", "delete")
                .put("path", "organisation")
                .put("value", RandomStringUtils.randomAlphabetic(45))
                .build();
        thrown.expect(InvalidOperationException.class);
        thrown.expectMessage("Operation(s) are invalid: [delete]");
        thrown.reportMissingExceptionWithMessage("InvalidOperationException expected");
        validator.validatePatchRequest("an-externalId", of(request1, request2));
    }
    
    @Test
    public void shouldThrowInvalidFieldsException_whenValueIsMissing() {
        ImmutableMap<String, String> request1 = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "access_token")
                .put("value", "   ")
                .build();
        ImmutableMap<String, String> request2 = ImmutableMap.<String, String>builder()
                .put("op", "delete")
                .put("path", "organisation")
                .put("value", RandomStringUtils.randomAlphabetic(45))
                .build();
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [value]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        validator.validatePatchRequest("an-external-id", of(request1, request2));
    }

    @Test
    public void shouldThrowBadRequestException_whenPayloadContainsThreeOperations() {
        ImmutableMap<String, String> map1 = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "organisation")
                .put("value", "1234abcde")
                .build();
        ImmutableMap<String, String> map2 = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "access_token")
                .put("value", "abcde1234")
                .build();
        ImmutableMap<String, String> map3 =
                ImmutableMap.<String, String>builder()
                        .put("op", "replace")
                        .put("path", "organisation")
                        .put("value", "1234abcde")
                        .build();
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Patch payload contains wrong size of operations (3)");
        thrown.reportMissingExceptionWithMessage("BadRequestException expected");
        validator.validatePatchRequest("an-external_id", of(map1, map2, map3));
    }

    @Test
    public void shouldThrowBadRequestException_whenPayloadContainsNoOperations() {
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Patch payload contains no operations");
        thrown.reportMissingExceptionWithMessage("BadRequestException expected");
        validator.validatePatchRequest("an-external-id", null);
    }
}
