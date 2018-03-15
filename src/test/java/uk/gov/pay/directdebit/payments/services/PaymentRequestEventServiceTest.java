package uk.gov.pay.directdebit.payments.services;

import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture.*;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type.CHARGE;

@RunWith(MockitoJUnitRunner.class)
public class PaymentRequestEventServiceTest {

    @Mock
    private PaymentRequestEventDao mockedPaymentRequestEventDao;

    private PaymentRequestEventService service;

    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture();
    @Before
    public void setUp() {
        service = new PaymentRequestEventService(mockedPaymentRequestEventDao);
    }

    @Test
    public void registerTokenExchangedEventFor_shouldInsertAnEventWhenTokenIsExchanged() {
        service.registerTokenExchangedEventFor(transactionFixture.toEntity());
        ArgumentCaptor<PaymentRequestEvent> prCaptor = forClass(PaymentRequestEvent.class);
        verify(mockedPaymentRequestEventDao).insert(prCaptor.capture());
        PaymentRequestEvent paymentRequestEvent = prCaptor.getValue();
        assertThat(paymentRequestEvent.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(paymentRequestEvent.getEvent(), is(PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED));
        assertThat(paymentRequestEvent.getEventType(), is(CHARGE));
        assertThat(paymentRequestEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerDirectDebitReceivedEventFor_shouldInsertAnEventWhenDDDetailsAreReceived() {
        service.registerDirectDebitReceivedEventFor(transactionFixture.toEntity());
        ArgumentCaptor<PaymentRequestEvent> prCaptor = forClass(PaymentRequestEvent.class);
        verify(mockedPaymentRequestEventDao).insert(prCaptor.capture());
        PaymentRequestEvent paymentRequestEvent = prCaptor.getValue();
        assertThat(paymentRequestEvent.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(paymentRequestEvent.getEvent(), is(PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED));
        assertThat(paymentRequestEvent.getEventType(), is(CHARGE));
        assertThat(paymentRequestEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPayerCreatedEventFor_shouldInsertAnEventWhenPayerIsCreated() {
        service.registerPayerCreatedEventFor(transactionFixture.toEntity());
        ArgumentCaptor<PaymentRequestEvent> prCaptor = forClass(PaymentRequestEvent.class);
        verify(mockedPaymentRequestEventDao).insert(prCaptor.capture());
        PaymentRequestEvent paymentRequestEvent = prCaptor.getValue();
        assertThat(paymentRequestEvent.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(paymentRequestEvent.getEvent(), is(PaymentRequestEvent.SupportedEvent.PAYER_CREATED));
        assertThat(paymentRequestEvent.getEventType(), is(PaymentRequestEvent.Type.PAYER));
        assertThat(paymentRequestEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentCreatedEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentCreatedEventFor(transactionFixture.toEntity());
        ArgumentCaptor<PaymentRequestEvent> prCaptor = forClass(PaymentRequestEvent.class);
        verify(mockedPaymentRequestEventDao).insert(prCaptor.capture());
        PaymentRequestEvent paymentRequestEvent = prCaptor.getValue();
        assertThat(paymentRequestEvent.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(paymentRequestEvent.getEvent(), is(PaymentRequestEvent.SupportedEvent.PAYMENT_CREATED));
        assertThat(paymentRequestEvent.getEventType(), is(CHARGE));
        assertThat(paymentRequestEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void registerPaymentPendingEventFor_shouldCreateExpectedEvent() {
        service.registerPaymentPendingEventFor(transactionFixture.toEntity());
        ArgumentCaptor<PaymentRequestEvent> prCaptor = forClass(PaymentRequestEvent.class);
        verify(mockedPaymentRequestEventDao).insert(prCaptor.capture());
        PaymentRequestEvent paymentRequestEvent = prCaptor.getValue();
        assertThat(paymentRequestEvent.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(paymentRequestEvent.getEvent(), is(PAYMENT_PENDING));
        assertThat(paymentRequestEvent.getEventType(), is(CHARGE));
        assertThat(paymentRequestEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void findBy_shouldFindEvent() {
        long paymentRequestId = 1L;
        PaymentRequestEvent expectedEvent = aPaymentRequestEventFixture().toEntity();
        when(mockedPaymentRequestEventDao.findByPaymentRequestIdAndEvent(paymentRequestId, "CHARGE", "PAYMENT_PENDING"))
                .thenReturn(Optional.of(expectedEvent));

        Optional<PaymentRequestEvent> event = service.findBy(paymentRequestId, CHARGE, PAYMENT_PENDING);

        assertThat(event.get(), is(expectedEvent));
    }
}


/*

   public PaymentRequestEvent registerPaymentCreatedEventFor(Transaction charge) {
        PaymentRequestEvent event = paymentCreated(charge.getPaymentRequestId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPaymentPendingEventFor(Transaction charge) {
        PaymentRequestEvent event = paymentPending(charge.getPaymentRequestId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPaymentPaidOutEventFor(Transaction charge) {
        PaymentRequestEvent event = paidOut(charge.getPaymentRequestId());
        return insertEventFor(charge, event);
    }

    public void registerTokenExchangedEventFor(Transaction charge) {
        PaymentRequestEvent event = tokenExchanged(charge.getPaymentRequestId());
        insertEventFor(charge, event);
    }

    public Optional<PaymentRequestEvent> findBy(Long paymentRequestId, PaymentRequestEvent.Type type, PaymentRequestEvent.SupportedEvent event) {
        return paymentRequestEventDao.findByPaymentRequestIdAndEvent(paymentRequestId, type.toString(), event.toString());
    }

*/
