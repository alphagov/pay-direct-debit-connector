package uk.gov.pay.directdebit.common.exception.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ExternalMandateStateValidator.class})
@Documented
public @interface ValidExternalMandateState {

    String message() default "Must be a valid mandate external state";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
