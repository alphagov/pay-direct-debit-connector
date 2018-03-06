package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture.aGoCardlessMandateFixture;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture.aGoCardlessPaymentFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)

public class WebhookGoCardlessServiceTest {

    @Mock
    GoCardlessEventDao mockedGoCardlessEventDao;

    @Mock
    GoCardlessService mockedGoCardlessService;

    @Mock
    TransactionService mockedTransactionService;

    WebhookGoCardlessService webhookGoCardlessService;
    @Before
    public void setUp() {
        webhookGoCardlessService = new WebhookGoCardlessService(mockedGoCardlessEventDao, mockedGoCardlessService, mockedTransactionService);
    }

    @Test
    public void shouldInsertEvents() {
        GoCardlessEvent firstGoCardlessEvent = aGoCardlessEventFixture().toEntity();
        GoCardlessEvent secondGoCardlessEvent = aGoCardlessEventFixture().toEntity();
        List<GoCardlessEvent> events = Arrays.asList(firstGoCardlessEvent, secondGoCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventDao).insert(firstGoCardlessEvent);
        verify(mockedGoCardlessEventDao).insert(secondGoCardlessEvent);
    }

    @Test
    public void shouldHandlePaymentEventsWithAValidAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType("payments").withAction("paid_out").toEntity();
        GoCardlessPayment goCardlessPayment = aGoCardlessPaymentFixture().toEntity();
        Transaction transaction = aTransactionFixture().toEntity();
        PaymentRequestEvent paymentRequestEvent = PaymentRequestEvent.paidOut(transaction.getPaymentRequestId());
        when(mockedGoCardlessService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPayment);
        when(mockedTransactionService.findChargeFor(goCardlessPayment.getTransactionId())).thenReturn(transaction);
        when(mockedTransactionService.paidOutFor(transaction)).thenReturn(paymentRequestEvent);

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventDao).insert(goCardlessEvent);
        verify(mockedGoCardlessEventDao).updatePaymentRequestEventId(goCardlessEvent.getId(), paymentRequestEvent.getId());
    }

    @Test
    public void shouldNotHandleMandateEventsWithAnInvalidResource() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType("not_handled").withAction("created").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);

        verify(mockedGoCardlessEventDao).insert(goCardlessEvent);
        verifyZeroInteractions(mockedGoCardlessService);
        verifyZeroInteractions(mockedTransactionService);
    }

    @Test
    public void shouldNotHandlePaymentEventsWithAnInvalidAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType("payments").withAction("not_handled").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventDao).insert(goCardlessEvent);
        verifyZeroInteractions(mockedGoCardlessService);
        verifyZeroInteractions(mockedTransactionService);
    }

    @Test
    public void shouldNotHandleMandateEventsWithAnInvalidAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType("mandates").withAction("not_handled").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventDao).insert(goCardlessEvent);
        verifyZeroInteractions(mockedGoCardlessService);
        verifyZeroInteractions(mockedTransactionService);
    }
    @Test
    public void shouldHandleMandateEventsWithAValidAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType("mandates").withAction("created").toEntity();
        GoCardlessMandate goCardlessMandate = aGoCardlessMandateFixture().toEntity();
        Transaction transaction = aTransactionFixture().toEntity();
        PaymentRequestEvent paymentRequestEvent = PaymentRequestEvent.mandateCreated(transaction.getPaymentRequestId());
        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandate);
        when(mockedTransactionService.findChargeForMandateId(goCardlessMandate.getMandateId())).thenReturn(transaction);
        when(mockedTransactionService.mandateCreatedFor(transaction)).thenReturn(paymentRequestEvent);

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventDao).insert(goCardlessEvent);
        verify(mockedGoCardlessEventDao).updatePaymentRequestEventId(goCardlessEvent.getId(), paymentRequestEvent.getId());
    }
}
