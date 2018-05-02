package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessMandateHandlerTest {

    @Mock
    private TransactionService mockedTransactionService;

    @Mock
    private MandateService mockedMandateService;

    @Mock
    private GoCardlessService mockedGoCardlessService;

    @Mock
    private PayerService mockedPayerService;

    @Captor
    private ArgumentCaptor<GoCardlessEvent> geCaptor;

    private PaymentRequestEvent paymentRequestEvent = PaymentRequestEventFixture.aPaymentRequestEventFixture().toEntity();
    private GoCardlessMandateFixture goCardlessMandateFixture = GoCardlessMandateFixture.aGoCardlessMandateFixture();
    private Payer payer = PayerFixture.aPayerFixture().toEntity();
    private Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();
    private GoCardlessMandateHandler goCardlessMandateHandler;

    @Before
    public void setUp() {
        goCardlessMandateHandler = new GoCardlessMandateHandler(mockedTransactionService, mockedGoCardlessService, mockedPayerService, mockedMandateService);
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldRegisterEventAsMandatePending_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("created").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);

        when(mockedTransactionService.findMandatePendingEventFor(transaction)).thenReturn(Optional.empty());
        when(mockedMandateService.mandatePendingFor(transaction)).thenReturn(paymentRequestEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);
        verify(goCardlessEvent).setPaymentRequestEventId(paymentRequestEvent.getId());
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldNotRegisterAnEventAsMandatePending_whenAPreviousMandatePendingEventExists() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("created").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);

        when(mockedTransactionService.findMandatePendingEventFor(transaction)).thenReturn(Optional.of(paymentRequestEvent));

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedMandateService, never()).mandatePendingFor(transaction);
        verify(goCardlessEvent).setPaymentRequestEventId(paymentRequestEvent.getId());
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onActiveMandateGoCardlessEvent_shouldSetAPayEventAsMandateActive_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("active").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);
        when(mockedMandateService.mandateActiveFor(transaction)).thenReturn(paymentRequestEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setPaymentRequestEventId(paymentRequestEvent.getId());
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onActiveMandateGoCardlessEvent_shouldNotRegisterAnEventAsMandatePending_whenAPreviousMandatePendingEventExists() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("active").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);

        when(mockedMandateService.mandateActiveFor(transaction)).thenReturn(paymentRequestEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setPaymentRequestEventId(paymentRequestEvent.getId());
        verify(mockedMandateService, never()).mandatePendingFor(transaction);
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onSubmittedMandateGoCardlessEvent_shouldSetAPayEventAsMandatePending_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("submitted").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);
        when(mockedMandateService.mandatePendingFor(transaction)).thenReturn(paymentRequestEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setPaymentRequestEventId(paymentRequestEvent.getId());
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());

        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onSubmittedMandateGoCardlessEvent_shouldNotRegisterAnEventAsMandatePending_whenAPreviousMandatePendingEventExists() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("submitted").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);

        when(mockedTransactionService.findMandatePendingEventFor(transaction)).thenReturn(Optional.of(paymentRequestEvent));

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setPaymentRequestEventId(paymentRequestEvent.getId());
        verify(mockedMandateService, never()).mandatePendingFor(transaction);
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());

        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onAFailedMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateFailed() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("failed").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedPayerService.getPayerFor(transaction)).thenReturn(payer);
        when(mockedMandateService.mandateFailedFor(transaction, payer)).thenReturn(paymentRequestEvent);
        when(mockedTransactionService.paymentFailedFor(transaction, payer, false)).thenReturn(paymentRequestEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentFailedFor(transaction, payer, false);
        verify(mockedMandateService).mandateFailedFor(transaction, payer);
        verify(goCardlessEvent).setPaymentRequestEventId(paymentRequestEvent.getId());
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onACancelledMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateCancelled_AndFailPaymentIfNotSubmittedToGC() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("cancelled").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedPayerService.getPayerFor(transaction)).thenReturn(payer);
        when(mockedTransactionService.findPaymentSubmittedEventFor(transaction)).thenReturn(Optional.empty());
        when(mockedMandateService.mandateCancelledFor(transaction, payer)).thenReturn(paymentRequestEvent);
        when(mockedTransactionService.paymentFailedFor(transaction, payer, false)).thenReturn(paymentRequestEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentFailedFor(transaction, payer, false);
        verify(mockedMandateService).mandateCancelledFor(transaction, payer);
        verify(goCardlessEvent).setPaymentRequestEventId(paymentRequestEvent.getId());
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onACancelledMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateCancelled_withoutFailingPaymentIfSubmittedToGC() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("cancelled").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedPayerService.getPayerFor(transaction)).thenReturn(payer);
        when(mockedTransactionService.findPaymentSubmittedEventFor(transaction)).thenReturn(Optional.of(paymentRequestEvent));
        when(mockedMandateService.mandateCancelledFor(transaction, payer)).thenReturn(paymentRequestEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService, never()).paymentFailedFor(transaction, payer, false);
        verify(mockedMandateService).mandateCancelledFor(transaction, payer);
        verify(goCardlessEvent).setPaymentRequestEventId(paymentRequestEvent.getId());
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void shouldStoreEventsWithAnUnhandledAction() {
        GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().withAction("somethingelse").toEntity();
        goCardlessMandateHandler.handle(goCardlessEvent);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
    }
}
