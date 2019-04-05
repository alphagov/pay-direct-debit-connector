package uk.gov.pay.directdebit.gatewayaccounts.api;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CreateGatewayAccountRequestTest {

    private Validator validator;
    private final String tooLong = "This String is over 255 characters long. This String is over 255 characters long. This String is over 255 characters long. This String is over 255 characters long." +
            " This String is over 255 characters long. This String is over 255 characters long. This String is over 255 characters long. This String is over 255 characters long." +
            " This String is over 255 characters long. This String is over 255 characters long. This String is over 255 characters long. This String is over 255 characters long."  +
            " This String is over 255 characters long. This String is over 255 characters long. This String is over 255 characters long. This String is over 255 characters long.";

    @Before
    public void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void shouldGiveConstraintViolationForServiceNameTooLong() {
        String longServiceName = "THIS SERVICE NAME IS GREATER THAN 50 CHARACTERS AND SHOULD NOT BE PERMITTED";
        Set<ConstraintViolation<CreateGatewayAccountRequest>> violations = validator.validateValue(CreateGatewayAccountRequest.class, "serviceName", longServiceName);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("service_name must be between 1 and 50 characters."));
    }

    @Test
    public void shouldGiveConstraintViolationForNullPaymentProvider() {
        Set<ConstraintViolation<CreateGatewayAccountRequest>> violations = validator.validateValue(CreateGatewayAccountRequest.class, "paymentProvider", null);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("may not be null"));
    }

    @Test
    public void shouldGiveConstraintViolationForNullType() {
        Set<ConstraintViolation<CreateGatewayAccountRequest>> violations = validator.validateValue(CreateGatewayAccountRequest.class, "type", null);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("may not be null"));
    }

    @Test
    public void shouldGiveConstraintViolationForTooLongDescription() {
        Set<ConstraintViolation<CreateGatewayAccountRequest>> violations = validator.validateValue(CreateGatewayAccountRequest.class, "description", tooLong);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("size must be between 0 and 255"));
    }

    @Test
    public void shouldGiveConstraintViolationForTooLongAnalysticsId() {
        Set<ConstraintViolation<CreateGatewayAccountRequest>> violations = validator.validateValue(CreateGatewayAccountRequest.class, "analyticsId", tooLong);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("size must be between 0 and 255"));
    }

    @Test
    public void shouldPassValidation() {
        CreateGatewayAccountRequest createGatewayAccountRequest = new CreateGatewayAccountRequest(
                PaymentProvider.SANDBOX,
                "a service name",
                GatewayAccount.Type.TEST,
                null,
                null,
                null,
                null);

        Set<ConstraintViolation<CreateGatewayAccountRequest>> violations = validator.validate(createGatewayAccountRequest);
        assertThat(violations.size(), is(0));
    }

}