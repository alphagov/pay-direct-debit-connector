package uk.gov.pay.directdebit.payments.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.exception.UnsupportedPaymentRequestEventException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAID;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYER_CREATED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_ACKNOWLEDGED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_FAILED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.payments.model.Event.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.Event.Type.MANDATE;
import static uk.gov.pay.directdebit.payments.model.Event.Type.PAYER;

public class EventTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldGetPaymentEventFromString() throws UnsupportedPaymentRequestEventException {
        assertThat(Event.SupportedEvent.fromString("TOKEN_EXCHANGED"), is(TOKEN_EXCHANGED));
    }

    @Test
    public void shouldThrowExceptionIfUnknownEvent() throws UnsupportedPaymentRequestEventException {
        thrown.expect(Exception.class);
        thrown.expectMessage("Event \"blabla\" is not supported");
        thrown.reportMissingExceptionWithMessage("UnknownPaymentRequestEventException expected");
        Event.SupportedEvent.fromString("blabla");
    }

    @Test
    public void directDebitDetailsConfirmed_shouldReturnExpectedEvent() {
        long mandateId = 2L;
        Event event = Event.directDebitDetailsConfirmed(mandateId);

        assertThat(event.getEvent(), is(DIRECT_DEBIT_DETAILS_CONFIRMED));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(nullValue()));
    }

    @Test
    public void payerCreated_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        Event event = Event.payerCreated(mandateId);

        assertThat(event.getEvent(), is(PAYER_CREATED));
        assertThat(event.getEventType(), is(PAYER));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(nullValue()));
    }

    @Test
    public void directDebitDetailsReceived_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        Event event = Event.directDebitDetailsReceived(mandateId);

        assertThat(event.getEvent(), is(DIRECT_DEBIT_DETAILS_RECEIVED));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(nullValue()));
    }

    @Test
    public void tokenExchanged_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        Event event = Event.tokenExchanged(mandateId);

        assertThat(event.getEvent(), is(TOKEN_EXCHANGED));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(nullValue()));
    }

    @Test
    public void chargeCreated_shouldReturnExpectedEvent() {
        long paymentRequestId = 1L;
        long mandateId = 2L;
        Event event = Event.chargeCreated(mandateId, paymentRequestId);

        assertThat(event.getEvent(), is(CHARGE_CREATED));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(paymentRequestId));
    }

    @Test
    public void paidOut_shouldReturnExpectedEvent() {
        long paymentRequestId = 1L;
        long mandateId = 2L;
        Event event = Event.paidOut(mandateId, paymentRequestId);

        assertThat(event.getEvent(), is(PAID_OUT));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(paymentRequestId));
    }
    @Test
    public void paymentSubmittedToProvider_shouldReturnExpectedEvent() {
        long paymentRequestId = 1L;
        long mandateId = 2L;
        Event event = Event.paymentSubmittedToProvider(mandateId, paymentRequestId);

        assertThat(event.getEvent(), is(PAYMENT_SUBMITTED_TO_PROVIDER));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(paymentRequestId));
    }

    @Test
    public void paymentAcknowlegedByProvider_shouldReturnExpectedEvent() {
        long paymentRequestId = 1L;
        long mandateId = 2L;
        Event event = Event.paymentAcknowledged(mandateId, paymentRequestId);

        assertThat(event.getEvent(), is(PAYMENT_ACKNOWLEDGED_BY_PROVIDER));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(paymentRequestId));
    }

    @Test
    public void paymentSubmittedToBank_shouldReturnExpectedEvent() {
        long paymentRequestId = 1L;
        long mandateId = 2L;
        Event event = Event.paymentSubmitted(mandateId, paymentRequestId);

        assertThat(event.getEvent(), is(PAYMENT_SUBMITTED_TO_BANK));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(paymentRequestId));
    }

    @Test
    public void paymentFailed_shouldReturnExpectedEvent() {
        long paymentRequestId = 1L;
        long mandateId = 2L;
        Event event = Event.paymentFailed(mandateId, paymentRequestId);

        assertThat(event.getEvent(), is(PAYMENT_FAILED));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(paymentRequestId));
    }

    @Test
    public void paymentCancelled_shouldReturnExpectedEvent() {
        long paymentRequestId = 1L;
        long mandateId = 2L;
        Event event = Event.paymentCancelled(mandateId, paymentRequestId);

        assertThat(event.getEvent(), is(PAYMENT_CANCELLED_BY_USER));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(paymentRequestId));
    }

    @Test
    public void payoutPaid_shouldReturnExpectedEvent() {
        long paymentRequestId = 1L;
        long mandateId = 2L;
        Event event = Event.payoutPaid(mandateId, paymentRequestId);

        assertThat(event.getEvent(), is(PAID));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getMandateId(), is(mandateId));
        assertThat(event.getTransactionId(), is(paymentRequestId));
    }

    @Test
    public void mandateActive_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        Event event = Event.mandateActive(mandateId);

        assertThat(event.getEvent(), is(MANDATE_ACTIVE));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getMandateId(), is(mandateId));
    }

    @Test
    public void mandatePending_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        Event event = Event.mandatePending(mandateId);

        assertThat(event.getEvent(), is(MANDATE_PENDING));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getMandateId(), is(mandateId));
    }

    @Test
    public void mandateFailed_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        Event event = Event.mandateFailed(mandateId);

        assertThat(event.getEvent(), is(MANDATE_FAILED));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getMandateId(), is(mandateId));
    }

    @Test
    public void mandateCancelled_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        Event event = Event.mandateCancelled(mandateId);

        assertThat(event.getEvent(), is(MANDATE_CANCELLED));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getMandateId(), is(mandateId));
    }

    @Test
    public void paymentMethodChanged_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        Event event = Event.paymentMethodChanged(mandateId);

        assertThat(event.getEvent(), is(PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getMandateId(), is(mandateId));
    }
}
