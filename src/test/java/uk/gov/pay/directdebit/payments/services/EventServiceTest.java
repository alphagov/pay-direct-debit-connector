package uk.gov.pay.directdebit.payments.services;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.dao.EventDao;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Event;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAID;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_ACKNOWLEDGED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_FAILED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.Event.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.Event.Type.MANDATE;

@RunWith(MockitoJUnitRunner.class)
public class EventServiceTest {

    @Mock
    private EventDao mockedEventDao;

    @Captor
    private ArgumentCaptor<Event> prCaptor;

    private PaymentRequestEventService service;

    private MandateFixture mandateFixture = MandateFixture.aMandateFixture();
    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture);

    @Before
    public void setUp() {
        service = new PaymentRequestEventService(mockedEventDao);
    }

    @Test
    public void registerTokenExchangedEventFor_shouldInsertAnEventWhenTokenIsExchanged() {
        service.registerTokenExchangedEventFor(mandateFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(nullValue()));
        assertThat(event.getEvent(), is(Event.SupportedEvent.TOKEN_EXCHANGED));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerDirectDebitReceivedEventFor_shouldInsertAnEventWhenDDDetailsAreReceived() {
        service.registerDirectDebitReceivedEventFor(mandateFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(nullValue()));
        assertThat(event.getEvent(), is(Event.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPayerCreatedEventFor_shouldInsertAnEventWhenPayerIsCreated() {
        service.registerPayerCreatedEventFor(mandateFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(nullValue()));
        assertThat(event.getEvent(), is(Event.SupportedEvent.PAYER_CREATED));
        assertThat(event.getEventType(), is(Event.Type.PAYER));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPayerEditedEventFor_shouldInsertAnEventWhenPayerIsEdited() {
        service.registerPayerEditedEventFor(mandateFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(nullValue()));
        assertThat(event.getEvent(), is(Event.SupportedEvent.PAYER_EDITED));
        assertThat(event.getEventType(), is(Event.Type.PAYER));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentCreatedEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentSubmittedToProviderEventFor(transactionFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(transactionFixture.getId()));
        assertThat(event.getEvent(), is(Event.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentPendingEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentAcknowledgedEventFor(transactionFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(transactionFixture.getId()));
        assertThat(event.getEvent(), is(PAYMENT_ACKNOWLEDGED_BY_PROVIDER));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentSubmittedEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentSubmittedEventFor(transactionFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(transactionFixture.getId()));
        assertThat(event.getEvent(), is(PAYMENT_SUBMITTED_TO_BANK));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentPaidOutEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentPaidOutEventFor(transactionFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(transactionFixture.getId()));
        assertThat(event.getEvent(), is(PAID_OUT));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentCancelledEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentCancelledEventFor(mandateFixture.toEntity(), transactionFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(transactionFixture.getId()));
        assertThat(event.getEvent(), is(PAYMENT_CANCELLED_BY_USER));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentFailedEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentFailedEventFor(transactionFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(transactionFixture.getId()));
        assertThat(event.getEvent(), is(PAYMENT_FAILED));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPayoutPaidEventFor_shouldCreateExpectedEvent() {
        service.registerPayoutPaidEventFor(transactionFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(transactionFixture.getId()));
        assertThat(event.getEvent(), is(PAID));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerMandatePendingEventFor_shouldCreateExpectedEvent() {
        service.registerMandatePendingEventFor(mandateFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(nullValue()));
        assertThat(event.getEvent(), is(MANDATE_PENDING));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerMandateActiveEventFor_shouldCreateExpectedEvent() {
        service.registerMandateActiveEventFor(mandateFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(nullValue()));
        assertThat(event.getEvent(), is(MANDATE_ACTIVE));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerMandateFailedEventFor_shouldCreateExpectedEvent() {
        service.registerMandateFailedEventFor(mandateFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(nullValue()));
        assertThat(event.getEvent(), is(MANDATE_FAILED));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerMandateCancelledEventFor_shouldCreateExpectedEvent() {
        service.registerMandateCancelledEventFor(mandateFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(nullValue()));
        assertThat(event.getEvent(), is(MANDATE_CANCELLED));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentMethodChangedEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentMethodChangedEventFor(mandateFixture.toEntity());

        verify(mockedEventDao).insert(prCaptor.capture());
        Event event = prCaptor.getValue();
        assertThat(event.getMandateId(), is(mandateFixture.getId()));
        assertThat(event.getTransactionId(), is(nullValue()));
        assertThat(event.getEvent(), is(PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }
}
