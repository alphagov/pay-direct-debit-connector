package uk.gov.pay.directdebit.webhook.gocardless.services;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
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

    @Captor
    private ArgumentCaptor<GoCardlessEvent> geCaptor;

    private DirectDebitEvent directDebitEvent = DirectDebitEventFixture.aDirectDebitEventFixture().toEntity();
    private PayerFixture payerFixture = PayerFixture.aPayerFixture();

    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withPayerFixture(payerFixture);
    private GoCardlessMandateFixture goCardlessMandateFixture = GoCardlessMandateFixture.aGoCardlessMandateFixture().withMandateId(mandateFixture.getId());
    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture);
    private GoCardlessMandateHandler goCardlessMandateHandler;

    @Before
    public void setUp() {
        goCardlessMandateHandler = new GoCardlessMandateHandler(mockedTransactionService, mockedGoCardlessService, mockedMandateService);
        when(mockedTransactionService.findTransactionsForMandate(mandateFixture.getExternalId())).thenReturn(ImmutableList
                .of(transactionFixture.toEntity()));
        when(mockedMandateService.findById(mandateFixture.getId())).thenReturn(mandateFixture.toEntity());
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldRegisterEventAsMandatePending_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("created").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedMandateService.mandatePendingFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldNotRegisterAnEventAsMandatePending_whenAPreviousMandatePendingEventExists() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("created").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedMandateService.findMandatePendingEventFor(mandateFixture.toEntity())).thenReturn(Optional.of(
                directDebitEvent));
        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedMandateService, never()).mandatePendingFor(mandateFixture.toEntity());
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onActiveMandateGoCardlessEvent_shouldSetAPayEventAsMandateActive_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("active").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedMandateService.mandateActiveFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);
        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onActiveMandateGoCardlessEvent_shouldNotRegisterAnEventAsMandatePending_whenAPreviousMandatePendingEventExists() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("active").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());

        when(mockedMandateService.mandateActiveFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedMandateService, never()).mandatePendingFor(mandateFixture.toEntity());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onSubmittedMandateGoCardlessEvent_shouldSetAPayEventAsMandatePending_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("submitted").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedMandateService.mandatePendingFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onSubmittedMandateGoCardlessEvent_shouldNotRegisterAnEventAsMandatePending_whenAPreviousMandatePendingEventExists() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("submitted").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedMandateService.findMandatePendingEventFor(mandateFixture.toEntity())).thenReturn(Optional.of(
                directDebitEvent));
        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedMandateService, never()).mandatePendingFor(mandateFixture.toEntity());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onAFailedMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateFailed() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("failed").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedMandateService.mandateFailedFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);
        when(mockedTransactionService.paymentFailedWithoutEmailFor(transactionFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentFailedWithoutEmailFor(transactionFixture.toEntity());
        verify(mockedMandateService).mandateFailedFor(mandateFixture.toEntity());
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onACancelledMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateCancelled_AndFailPaymentIfNotSubmittedToGC() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("cancelled").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findPaymentSubmittedEventFor(transactionFixture.toEntity())).thenReturn(Optional.empty());
        when(mockedMandateService.mandateCancelledFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);
        when(mockedTransactionService.paymentFailedWithoutEmailFor(transactionFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentFailedWithoutEmailFor(transactionFixture.toEntity());
        verify(mockedMandateService).mandateCancelledFor(mandateFixture.toEntity());
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onACancelledMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateCancelled_withoutFailingPaymentIfSubmittedToGC() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("cancelled").toEntity());

        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findPaymentSubmittedEventFor(transactionFixture.toEntity())).thenReturn(Optional.of(
                directDebitEvent));
        when(mockedMandateService.mandateCancelledFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService, never()).paymentFailedWithoutEmailFor(transactionFixture.toEntity());
        verify(mockedMandateService).mandateCancelledFor(mandateFixture.toEntity());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }
}
