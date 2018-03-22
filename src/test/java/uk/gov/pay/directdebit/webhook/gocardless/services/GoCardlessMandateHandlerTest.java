package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture;
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
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessMandateHandlerTest {

    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private GoCardlessService mockedGoCardlessService;

    private GoCardlessMandateFixture goCardlessMandateFixture = GoCardlessMandateFixture.aGoCardlessMandateFixture();
    private GoCardlessMandateHandler goCardlessMandateHandler;

    @Before
    public void setUp() {
        goCardlessMandateHandler = new GoCardlessMandateHandler(mockedTransactionService, mockedGoCardlessService);
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldRegisterEventAsMandatePending_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().withAction("created").toEntity();
        PaymentRequestEvent paymentRequestEvent = PaymentRequestEventFixture.aPaymentRequestEventFixture().toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);

        when(mockedTransactionService.findMandatePendingEventFor(transaction)).thenReturn(Optional.empty());
        when(mockedTransactionService.mandatePendingFor(transaction)).thenReturn(paymentRequestEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);


        ArgumentCaptor<GoCardlessEvent> geCaptor = forClass(GoCardlessEvent.class);
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldNotRegisterAnEventAsMandatePending_whenAPreviousMandatePendingEventExists() {
        GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().withAction("created").toEntity();
        PaymentRequestEvent paymentRequestEvent = PaymentRequestEventFixture.aPaymentRequestEventFixture().toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);

        when(mockedTransactionService.findMandatePendingEventFor(transaction)).thenReturn(Optional.of(paymentRequestEvent));

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService, never()).mandatePendingFor(transaction);
        ArgumentCaptor<GoCardlessEvent> geCaptor = forClass(GoCardlessEvent.class);
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onSubmittedMandateGoCardlessEvent_shouldSetAPayEventAsMandatePending_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().withAction("submitted").toEntity();
        PaymentRequestEvent paymentRequestEvent = PaymentRequestEventFixture.aPaymentRequestEventFixture().toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);

        when(mockedTransactionService.findMandatePendingEventFor(transaction)).thenReturn(Optional.empty());
        when(mockedTransactionService.mandatePendingFor(transaction)).thenReturn(paymentRequestEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);


        ArgumentCaptor<GoCardlessEvent> geCaptor = forClass(GoCardlessEvent.class);
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onSubmittedMandateGoCardlessEvent_shouldNotRegisterAnEventAsMandatePending_whenAPreviousMandatePendingEventExists() {
        GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().withAction("submitted").toEntity();
        PaymentRequestEvent paymentRequestEvent = PaymentRequestEventFixture.aPaymentRequestEventFixture().toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);

        when(mockedTransactionService.findMandatePendingEventFor(transaction)).thenReturn(Optional.of(paymentRequestEvent));

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService, never()).mandatePendingFor(transaction);
        ArgumentCaptor<GoCardlessEvent> geCaptor = forClass(GoCardlessEvent.class);
        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void handle_onActiveMandateGoCardlessEvent_shouldSetAPayEventAsMandateActive() {
        GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().withAction("active").toEntity();
        PaymentRequestEvent paymentRequestEvent = PaymentRequestEventFixture.aPaymentRequestEventFixture().toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);
        when(mockedTransactionService.mandateActiveFor(transaction)).thenReturn(paymentRequestEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);


        ArgumentCaptor<GoCardlessEvent> geCaptor = forClass(GoCardlessEvent.class);
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
