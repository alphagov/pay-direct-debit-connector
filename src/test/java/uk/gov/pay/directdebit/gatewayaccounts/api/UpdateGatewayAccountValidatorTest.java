package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidSizeFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;
import uk.gov.pay.directdebit.common.validation.BankAccountDetailsValidator;

import java.util.HashMap;
import java.util.Map;

public class UpdateGatewayAccountValidatorTest {
    
    private UpdateGatewayAccountValidator validator = new UpdateGatewayAccountValidator();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldNotThrowExceptionIfRequestIsValid() {
        Map<String, String> request = ImmutableMap
                .of("access_token", "nlkdsjlkd79f2jjakssdalksd", "organisation", "12345678");
        validator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsException_whenMissingRequiredFields() {
        Map<String, String> request = new HashMap<>();
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [access_token, organisation]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        validator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsException_whenMissingAccessTokenField() {
        Map<String, String> request = ImmutableMap.of("organisation", "nlkdsjlkd79f2jjakssdalksd");
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [access_token]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        validator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsException_whenMissingOrganisationField() {
        Map<String, String> request = ImmutableMap.of("access_token", "nlkdsjlkd79f2jjakssdalksd");
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [organisation]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        validator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsException_whenAccessTokenFieldIsNull() {
        Map<String, String> request = new HashMap<>();
        request.put("access_token", null);
        request.put("organisation", "jkdjauionnxqkljsal");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [access_token]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        validator.validate(request);
    }
    
    @Test
    public void shouldThrowInvalidFieldsException_whenAccessTokenIsTooLong() {
        Map<String, String> request = ImmutableMap
                .of("access_token", RandomStringUtils.randomAlphabetic(256), "organisation", "12345678");
        thrown.expect(InvalidSizeFieldsException.class);
        thrown.expectMessage("The size of a field(s) is invalid: [access_token]");
        thrown.reportMissingExceptionWithMessage("InvalidSizeFieldsException expected");
        validator.validate(request);
    }
}
