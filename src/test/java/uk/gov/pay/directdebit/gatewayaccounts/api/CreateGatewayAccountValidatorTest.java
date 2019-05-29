package uk.gov.pay.directdebit.gatewayaccounts.api;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.common.exception.validation.InvalidFieldsException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.util.HashMap;
import java.util.Map;

public class CreateGatewayAccountValidatorTest {

    private CreateGatewayAccountValidator createGatewayAccountValidator = new CreateGatewayAccountValidator();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldNotThrowExceptionIfRequestIsValid() {
        Map<String, String> request = generateValidRequest();
        createGatewayAccountValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfMissingRequiredFields() {
        Map<String, String> request = new HashMap<>();
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [" +
                "payment_provider, " +
                "type" +
                "]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        createGatewayAccountValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfPaymentProviderFieldIsMissing() {
        Map<String, String> request = generateValidRequest();
        request.remove("payment_provider");
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [" +
                "payment_provider" +
                "]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        createGatewayAccountValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfPaymentProviderFieldIsEmptyString() {
        Map<String, String> request = generateValidRequest();
        request.put("payment_provider", "");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                "payment_provider" +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createGatewayAccountValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfPaymentProviderFieldIsNull() {
        Map<String, String> request = generateValidRequest();
        request.put("payment_provider", null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                "payment_provider" +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createGatewayAccountValidator.validate(request);
    }

    @Test
    public void shouldThrowMissingMandatoryFieldsExceptionIfTypeFieldIsMissing() {
        Map<String, String> request = generateValidRequest();
        request.remove("type");
        thrown.expect(MissingMandatoryFieldsException.class);
        thrown.expectMessage("Field(s) missing: [" +
                "type" +
                "]");
        thrown.reportMissingExceptionWithMessage("MissingMandatoryFieldsException expected");
        createGatewayAccountValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidSizeFieldsExceptionIfTypeFieldIsEmptyString() {
        Map<String, String> request = generateValidRequest();
        request.put("type", "");
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                "type" +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createGatewayAccountValidator.validate(request);
    }

    @Test
    public void shouldThrowInvalidFieldsExceptionIfTypeFieldIsNull() {
        Map<String, String> request = generateValidRequest();
        request.put("type", null);
        thrown.expect(InvalidFieldsException.class);
        thrown.expectMessage("Field(s) are invalid: [" +
                "type" +
                "]");
        thrown.reportMissingExceptionWithMessage("InvalidFieldsException expected");
        createGatewayAccountValidator.validate(request);
    }

    private Map<String, String> generateValidRequest() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
        Map<String, String> request = new HashMap<>();
        request.put("payment_provider", gatewayAccountFixture.getPaymentProvider().toString());
        request.put("type", gatewayAccountFixture.getType().toString());
        request.put("description", gatewayAccountFixture.getDescription());
        request.put("analytics_id", gatewayAccountFixture.getAnalyticsId());
        return request;
    }

}
