package uk.gov.pay.directdebit.payments.api;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.dao.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.exception.NegativeSearchParamException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PaymentViewValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private PaymentViewValidator validator = new PaymentViewValidator();

    @Test
    public void shouldReturnNoErrors() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account", 3l, 256l);
        validator.validateParams(searchParams);

    }

    @Test
    public void shouldReturnAnError_whenPageIsZero() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account", 0l, 256l);
        thrown.expect(NegativeSearchParamException.class);
        thrown.expectMessage("Query param 'page' should be a non zero positive integer");
        validator.validateParams(searchParams);
    }

    @Test
    public void shouldResetDisplaySizeTo500() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account", 2l, 600l);
        validator.validateParams(searchParams);
        assertThat(searchParams.getPaginationParams().getDisplaySize(), is(500l));
    }

    @Test
    public void shouldSetPaginationWithDefaultValues() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account", null, null);
        validator.validateParams(searchParams);
        assertThat(searchParams.getPaginationParams().getDisplaySize(), is(500l));
        assertThat(searchParams.getPaginationParams().getPageNumber(), is(1l));
    }
}
