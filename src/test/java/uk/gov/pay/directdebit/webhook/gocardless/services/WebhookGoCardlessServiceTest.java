package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture.aGoCardlessMandateFixture;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture.aGoCardlessPaymentFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.UNHANDLED;

@RunWith(MockitoJUnitRunner.class)

public class WebhookGoCardlessServiceTest {
    @Mock
    private GoCardlessService mockedGoCardlessService;
    @Mock
    private TransactionService mockedTransactionService;
    private WebhookGoCardlessService webhookGoCardlessService;
    private GoCardlessPayment goCardlessPayment = aGoCardlessPaymentFixture().toEntity();
    private Transaction transaction = aTransactionFixture().toEntity();
    private GoCardlessMandate goCardlessMandate = aGoCardlessMandateFixture().toEntity();

    @Before
    public void setUp() {
        webhookGoCardlessService = new WebhookGoCardlessService(mockedGoCardlessService, mockedTransactionService);
    }

    @Test
    public void shouldInsertEvents() {
        GoCardlessEvent firstGoCardlessEvent = aGoCardlessEventFixture().toEntity();
        GoCardlessEvent secondGoCardlessEvent = aGoCardlessEventFixture().toEntity();
        List<GoCardlessEvent> events = Arrays.asList(firstGoCardlessEvent, secondGoCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(firstGoCardlessEvent);
        verify(mockedGoCardlessService).storeEvent(secondGoCardlessEvent);
    }

    @Test
    public void shouldStoreEventsWithAnUnhandledResource() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(UNHANDLED).withAction("created").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
        verifyZeroInteractions(mockedTransactionService);
    }

    @Test
    public void shouldStoreButNotHandlePaymentEventsWithAnUnhandledAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(PAYMENTS).withAction("not_handled").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
        verifyZeroInteractions(mockedTransactionService);
    }

    @Test
    public void shouldStoreButNotHandleMandateEventsWithAnUnhandledAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(MANDATES).withAction("not_handled_again").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
        verifyZeroInteractions(mockedTransactionService);
    }

    @Test
    public void shouldStoreAndHandlePaymentEventsWithAValidAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(PAYMENTS).withAction("paid_out").toEntity();

        PaymentRequestEvent paymentRequestEvent = PaymentRequestEvent.paidOut(transaction.getPaymentRequestId());
        when(mockedGoCardlessService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPayment);
        when(mockedTransactionService.findTransactionFor(goCardlessPayment.getTransactionId())).thenReturn(transaction);
        when(mockedTransactionService.paymentPaidOutFor(transaction)).thenReturn(paymentRequestEvent);

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
        verify(mockedTransactionService).paymentPaidOutFor(transaction);
    }

    @Test
    public void shouldStoreAndHandleMandateEventsWithAValidAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(MANDATES).withAction("created").toEntity();

        PaymentRequestEvent paymentRequestEvent = PaymentRequestEvent.paymentCreated(transaction.getPaymentRequestId());
        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandate);
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandate.getMandateId())).thenReturn(transaction);
        when(mockedTransactionService.findMandatePendingEventFor(transaction)).thenReturn(Optional.of(paymentRequestEvent));

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
        verify(mockedTransactionService).findMandatePendingEventFor(transaction);
    }
}
