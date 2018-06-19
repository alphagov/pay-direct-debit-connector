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
import uk.gov.pay.directdebit.payments.dao.DirectDebitEventDao;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_ACKNOWLEDGED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_FAILED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.MANDATE;

@RunWith(MockitoJUnitRunner.class)
public class DirectDebitEventServiceTest {

    @Mock
    private DirectDebitEventDao mockedDirectDebitEventDao;

    @Captor
    private ArgumentCaptor<DirectDebitEvent> prCaptor;

    private DirectDebitEventService service;

    private MandateFixture mandateFixture = MandateFixture.aMandateFixture();
    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture);

    @Before
    public void setUp() {
        service = new DirectDebitEventService(mockedDirectDebitEventDao);
    }

    @Test
    public void registerTokenExchangedEventFor_shouldInsertAnEventWhenTokenIsExchanged() {
        service.registerTokenExchangedEventFor(mandateFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
        assertThat(directDebitEvent.getEvent(), is(DirectDebitEvent.SupportedEvent.TOKEN_EXCHANGED));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerDirectDebitReceivedEventFor_shouldInsertAnEventWhenDDDetailsAreReceived() {
        service.registerDirectDebitReceivedEventFor(mandateFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
        assertThat(directDebitEvent.getEvent(), is(DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPayerCreatedEventFor_shouldInsertAnEventWhenPayerIsCreated() {
        service.registerPayerCreatedEventFor(mandateFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
        assertThat(directDebitEvent.getEvent(), is(DirectDebitEvent.SupportedEvent.PAYER_CREATED));
        assertThat(directDebitEvent.getEventType(), is(DirectDebitEvent.Type.PAYER));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPayerEditedEventFor_shouldInsertAnEventWhenPayerIsEdited() {
        service.registerPayerEditedEventFor(mandateFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
        assertThat(directDebitEvent.getEvent(), is(DirectDebitEvent.SupportedEvent.PAYER_EDITED));
        assertThat(directDebitEvent.getEventType(), is(DirectDebitEvent.Type.PAYER));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentCreatedEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentSubmittedToProviderEventFor(transactionFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(transactionFixture.getId()));
        assertThat(directDebitEvent.getEvent(), is(DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentPendingEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentAcknowledgedEventFor(transactionFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(transactionFixture.getId()));
        assertThat(directDebitEvent.getEvent(), is(PAYMENT_ACKNOWLEDGED_BY_PROVIDER));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentSubmittedEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentSubmittedEventFor(transactionFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(transactionFixture.getId()));
        assertThat(directDebitEvent.getEvent(), is(PAYMENT_SUBMITTED_TO_BANK));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentPaidOutEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentPaidOutEventFor(transactionFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(transactionFixture.getId()));
        assertThat(directDebitEvent.getEvent(), is(PAID_OUT));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentCancelledEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentCancelledEventFor(mandateFixture.toEntity(), transactionFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(transactionFixture.getId()));
        assertThat(directDebitEvent.getEvent(), is(PAYMENT_CANCELLED_BY_USER));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentFailedEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentFailedEventFor(transactionFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(transactionFixture.getId()));
        assertThat(directDebitEvent.getEvent(), is(PAYMENT_FAILED));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPayoutPaidEventFor_shouldCreateExpectedEvent() {
        service.registerPayoutPaidEventFor(transactionFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(transactionFixture.getId()));
        assertThat(directDebitEvent.getEvent(), is(PAID));
        assertThat(directDebitEvent.getEventType(), is(CHARGE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerMandatePendingEventFor_shouldCreateExpectedEvent() {
        service.registerMandatePendingEventFor(mandateFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
        assertThat(directDebitEvent.getEvent(), is(MANDATE_PENDING));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerMandateActiveEventFor_shouldCreateExpectedEvent() {
        service.registerMandateActiveEventFor(mandateFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
        assertThat(directDebitEvent.getEvent(), is(MANDATE_ACTIVE));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerMandateFailedEventFor_shouldCreateExpectedEvent() {
        service.registerMandateFailedEventFor(mandateFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
        assertThat(directDebitEvent.getEvent(), is(MANDATE_FAILED));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerMandateCancelledEventFor_shouldCreateExpectedEvent() {
        service.registerMandateCancelledEventFor(mandateFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
        assertThat(directDebitEvent.getEvent(), is(MANDATE_CANCELLED));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentMethodChangedEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentMethodChangedEventFor(mandateFixture.toEntity());

        verify(mockedDirectDebitEventDao).insert(prCaptor.capture());
        DirectDebitEvent directDebitEvent = prCaptor.getValue();
        assertThat(directDebitEvent.getMandateId(), is(mandateFixture.getId()));
        assertThat(directDebitEvent.getTransactionId(), is(nullValue()));
        assertThat(directDebitEvent.getEvent(), is(PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE));
        assertThat(directDebitEvent.getEventType(), is(MANDATE));
        assertThat(directDebitEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }
}
