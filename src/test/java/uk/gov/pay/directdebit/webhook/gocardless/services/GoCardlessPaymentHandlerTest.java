package uk.gov.pay.directdebit.webhook.gocardless.services;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessPaymentHandlerTest {

    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private PayerService mockedPayerService;
    @Mock
    private GoCardlessService mockedGoCardlessService;
    @Captor
    private ArgumentCaptor<GoCardlessEvent> geCaptor;

    private GoCardlessPaymentFixture goCardlessPaymentFixture = GoCardlessPaymentFixture.aGoCardlessPaymentFixture();
    private GoCardlessPaymentHandler goCardlessPaymentHandler;
    private DirectDebitEvent directDebitEvent = DirectDebitEventFixture.aDirectDebitEventFixture().toEntity();
    private Payer payer = PayerFixture.aPayerFixture().toEntity();
    private Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();

    @Before
    public void setUp() {
        goCardlessPaymentHandler = new GoCardlessPaymentHandler(mockedTransactionService, mockedGoCardlessService);
        when(mockedTransactionService.findTransaction(goCardlessPaymentFixture.getTransactionId())).thenReturn(transaction);
    }

    @Test
    public void handle_onPaidOutPaymentGoCardlessEvent_shouldSetAPayEventAsPaidOut() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("paid_out").toEntity());

        when(mockedGoCardlessService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.paymentPaidOutFor(transaction)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentPaidOutFor(transaction);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onCreatePaymentGoCardlessEvent_shouldSetAPayEventAsPaymentAcknowledged() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("created").toEntity());

        when(mockedGoCardlessService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.paymentAcknowledgedFor(transaction)).thenReturn(
                directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentAcknowledgedFor(transaction);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onPayoutPaidGoCardlessEvent_shouldSetRegisterPayoutPaidEvent() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("paid").withResourceType(GoCardlessResourceType.PAYOUTS).toEntity());

        when(mockedGoCardlessService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.payoutPaidFor(transaction)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).payoutPaidFor(transaction);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onSubmittedPaymentGoCardlessEvent_shouldSetRegisterPaymentSubmittedEvent() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("submitted").toEntity());

        when(mockedGoCardlessService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.paymentSubmittedFor(transaction)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentSubmittedFor(transaction);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onConfirmedPaymentGoCardlessEvent_shouldLinkTheEventToAnExistingPaymentSubmittedPayEvent() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("confirmed").toEntity());

        when(mockedGoCardlessService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.findPaymentSubmittedEventFor(transaction)).thenReturn(Optional.of(
                directDebitEvent));

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onFailedPaymentGoCardlessEvent_shouldSetAPayEventAsFailed() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("failed").toEntity());

        when(mockedGoCardlessService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.paymentFailedWithEmailFor(transaction)).thenReturn(
                directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentFailedWithEmailFor(transaction);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onPayoutPaidGoCardlessEvent_shouldLinkTheEventToAnExistingPaymentPaidOutPayEvent() {
        GoCardlessEvent goCardlessEvent = spy(GoCardlessEventFixture.aGoCardlessEventFixture().withAction("paid").toEntity());

        when(mockedGoCardlessService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.payoutPaidFor(transaction)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }
}
