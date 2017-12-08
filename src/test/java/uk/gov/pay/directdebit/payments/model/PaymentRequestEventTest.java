package uk.gov.pay.directdebit.payments.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.exception.UnsupportedPaymentRequestEventException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.WEBHOOK_ACTION_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.fromString;

public class PaymentRequestEventTest {


    @Test
    public void shouldGetPaymentEventFromString() throws UnsupportedPaymentRequestEventException {
        assertThat(fromString("WEBHOOK_ACTION_CONFIRMED"), is(WEBHOOK_ACTION_CONFIRMED));
    }
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfUnknownEvent() throws UnsupportedPaymentRequestEventException {
        thrown.expect(Exception.class);
        thrown.expectMessage("Event \"blabla\" is not supported");
        thrown.reportMissingExceptionWithMessage("UnknownPaymentRequestEventException expected");
        fromString("blabla");
    }
}
