package uk.gov.pay.directdebit.payments.services;

import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PaymentRequestEventServiceTest {

    @Mock
    private PaymentRequestEventDao mockedPaymentRequestEventDao;

    private PaymentRequestEventService service;

    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture();
    @Before
    public void setUp() throws Exception {
        service = new PaymentRequestEventService(mockedPaymentRequestEventDao);
    }

    @Test
    public void shouldInsertAnEventWhenTokenIsExchanged() {
        service.registerTokenExchangedEventFor(transactionFixture.toEntity());
        ArgumentCaptor<PaymentRequestEvent> prCaptor = forClass(PaymentRequestEvent.class);
        verify(mockedPaymentRequestEventDao).insert(prCaptor.capture());
        PaymentRequestEvent paymentRequestEvent = prCaptor.getValue();
        assertThat(paymentRequestEvent.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(paymentRequestEvent.getEvent(), is(PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED));
        assertThat(paymentRequestEvent.getEventType(), is(PaymentRequestEvent.Type.CHARGE));
        assertThat(paymentRequestEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void shouldInsertAnEventWhenDDDetailsAreReceived() {
        service.registerDirectDebitReceivedEventFor(transactionFixture.toEntity());
        ArgumentCaptor<PaymentRequestEvent> prCaptor = forClass(PaymentRequestEvent.class);
        verify(mockedPaymentRequestEventDao).insert(prCaptor.capture());
        PaymentRequestEvent paymentRequestEvent = prCaptor.getValue();
        assertThat(paymentRequestEvent.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(paymentRequestEvent.getEvent(), is(PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED));
        assertThat(paymentRequestEvent.getEventType(), is(PaymentRequestEvent.Type.CHARGE));
        assertThat(paymentRequestEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void shouldInsertAnEventWhenPayerIsCreated() {
        service.registerPayerCreatedEventFor(transactionFixture.toEntity());
        ArgumentCaptor<PaymentRequestEvent> prCaptor = forClass(PaymentRequestEvent.class);
        verify(mockedPaymentRequestEventDao).insert(prCaptor.capture());
        PaymentRequestEvent paymentRequestEvent = prCaptor.getValue();
        assertThat(paymentRequestEvent.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(paymentRequestEvent.getEvent(), is(PaymentRequestEvent.SupportedEvent.PAYER_CREATED));
        assertThat(paymentRequestEvent.getEventType(), is(PaymentRequestEvent.Type.PAYER));
        assertThat(paymentRequestEvent.getEventDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }
}
