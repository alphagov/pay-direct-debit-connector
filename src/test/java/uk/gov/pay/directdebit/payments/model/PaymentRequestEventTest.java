package uk.gov.pay.directdebit.payments.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.exception.UnsupportedPaymentRequestEventException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED;

public class PaymentRequestEventTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldGetPaymentEventFromString() throws UnsupportedPaymentRequestEventException {
        assertThat(PaymentRequestEvent.SupportedEvent.fromString("TOKEN_EXCHANGED"), is(TOKEN_EXCHANGED));
    }

    @Test
    public void shouldThrowExceptionIfUnknownEvent() throws UnsupportedPaymentRequestEventException {
        thrown.expect(Exception.class);
        thrown.expectMessage("Event \"blabla\" is not supported");
        thrown.reportMissingExceptionWithMessage("UnknownPaymentRequestEventException expected");
        PaymentRequestEvent.SupportedEvent.fromString("blabla");
    }
}
