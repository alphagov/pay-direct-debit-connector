package uk.gov.pay.directdebit.payments.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.exception.UnsupportedDirectDebitEventException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYER_CREATED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_ACKNOWLEDGED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_FAILED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.MANDATE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.PAYER;

public class DirectDebitEventTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldGetPaymentEventFromString() throws UnsupportedDirectDebitEventException {
        assertThat(DirectDebitEvent.SupportedEvent.fromString("TOKEN_EXCHANGED"), is(TOKEN_EXCHANGED));
    }

    @Test
    public void shouldThrowExceptionIfUnknownEvent() throws UnsupportedDirectDebitEventException {
        thrown.expect(Exception.class);
        thrown.expectMessage("Event \"blabla\" is not supported");
        thrown.reportMissingExceptionWithMessage("UnknownDirectDebitEventException expected");
        DirectDebitEvent.SupportedEvent.fromString("blabla");
    }

    @Test
    public void directDebitDetailsConfirmed_shouldReturnExpectedEvent() {
        long mandateId = 2L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.directDebitDetailsConfirmed(mandateId);

        assertThat(directDebitEvent.getEvent(), is(DIRECT_DEBIT_DETAILS_CONFIRMED));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
    }

    @Test
    public void payerCreated_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.payerCreated(mandateId);

        assertThat(directDebitEvent.getEvent(), is(PAYER_CREATED));
        assertThat(directDebitEvent.getEventType(), is(PAYER));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
    }

    @Test
    public void directDebitDetailsReceived_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.directDebitDetailsReceived(mandateId);

        assertThat(directDebitEvent.getEvent(), is(DIRECT_DEBIT_DETAILS_RECEIVED));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
    }

    @Test
    public void tokenExchanged_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.tokenExchanged(mandateId);

        assertThat(directDebitEvent.getEvent(), is(TOKEN_EXCHANGED));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
    }

    @Test
    public void chargeCreated_shouldReturnExpectedEvent() {
        long transactionId = 1L;
        long mandateId = 2L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent
                .chargeCreated(mandateId, transactionId);

        assertThat(directDebitEvent.getEvent(), is(CHARGE_CREATED));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(transactionId));
    }

    @Test
    public void paidOut_shouldReturnExpectedEvent() {
        long transactionId = 1L;
        long mandateId = 2L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.paidOut(mandateId, transactionId);

        assertThat(directDebitEvent.getEvent(), is(PAID_OUT));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(transactionId));
    }
    @Test
    public void paymentSubmittedToProvider_shouldReturnExpectedEvent() {
        long transactionId = 1L;
        long mandateId = 2L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent
                .paymentSubmittedToProvider(mandateId, transactionId);

        assertThat(directDebitEvent.getEvent(), is(PAYMENT_SUBMITTED_TO_PROVIDER));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(transactionId));
    }

    @Test
    public void paymentAcknowlegedByProvider_shouldReturnExpectedEvent() {
        long transactionId = 1L;
        long mandateId = 2L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent
                .paymentAcknowledged(mandateId, transactionId);

        assertThat(directDebitEvent.getEvent(), is(PAYMENT_ACKNOWLEDGED_BY_PROVIDER));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(transactionId));
    }

    @Test
    public void paymentSubmittedToBank_shouldReturnExpectedEvent() {
        long transactionId = 1L;
        long mandateId = 2L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent
                .paymentSubmitted(mandateId, transactionId);

        assertThat(directDebitEvent.getEvent(), is(PAYMENT_SUBMITTED_TO_BANK));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(transactionId));
    }

    @Test
    public void paymentFailed_shouldReturnExpectedEvent() {
        long transactionId = 1L;
        long mandateId = 2L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent
                .paymentFailed(mandateId, transactionId);

        assertThat(directDebitEvent.getEvent(), is(PAYMENT_FAILED));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(transactionId));
    }

    @Test
    public void paymentCancelled_shouldReturnExpectedEvent() {
        long transactionId = 1L;
        long mandateId = 2L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent
                .paymentCancelled(mandateId, transactionId);

        assertThat(directDebitEvent.getEvent(), is(PAYMENT_CANCELLED_BY_USER));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(transactionId));
    }

    @Test
    public void payoutPaid_shouldReturnExpectedEvent() {
        long transactionId = 1L;
        long mandateId = 2L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.payoutPaid(mandateId, transactionId);

        assertThat(directDebitEvent.getEvent(), is(PAID));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
        assertThat(directDebitEvent.getTransactionId(), is(transactionId));
    }

    @Test
    public void mandateActive_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.mandateActive(mandateId);

        assertThat(directDebitEvent.getEvent(), is(MANDATE_ACTIVE));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
    }

    @Test
    public void mandatePending_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.mandatePending(mandateId);

        assertThat(directDebitEvent.getEvent(), is(MANDATE_PENDING));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
    }

    @Test
    public void mandateFailed_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.mandateFailed(mandateId);

        assertThat(directDebitEvent.getEvent(), is(MANDATE_FAILED));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
    }

    @Test
    public void mandateCancelled_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.mandateCancelled(mandateId);

        assertThat(directDebitEvent.getEvent(), is(MANDATE_CANCELLED));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
    }

    @Test
    public void paymentMethodChanged_shouldReturnExpectedEvent() {
        long mandateId = 1L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.paymentMethodChanged(mandateId);

        assertThat(directDebitEvent.getEvent(), is(PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getMandateId(), is(mandateId));
    }
    
    @Test
    public void external_id_should_be_set() {
        long mandateId = 2L;
        DirectDebitEvent directDebitEvent = DirectDebitEvent.paymentMethodChanged(mandateId);
        assertNotNull(directDebitEvent.getExternalId());
    }
}
