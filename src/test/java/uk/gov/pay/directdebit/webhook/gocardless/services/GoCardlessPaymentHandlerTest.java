package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessEventService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessPaymentHandlerTest {

    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private GoCardlessEventService mockedGoCardlessEventService;
    @Captor
    private ArgumentCaptor<GoCardlessEvent> geCaptor;

    private GoCardlessPaymentFixture goCardlessPaymentFixture = GoCardlessPaymentFixture.aGoCardlessPaymentFixture();
    private GoCardlessPaymentHandler goCardlessPaymentHandler;
    private DirectDebitEvent directDebitEvent = DirectDebitEventFixture.aDirectDebitEventFixture().toEntity();
    private GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_org_id");
    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().withOrganisation(organisationIdentifier);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);
    private Transaction transaction = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture).toEntity();
    private GoCardlessEventFixture goCardlessEventFixture = GoCardlessEventFixture.aGoCardlessEventFixture().withOrganisationIdentifier(organisationIdentifier);
    
    @Before
    public void setUp() {
        goCardlessPaymentHandler = new GoCardlessPaymentHandler(mockedTransactionService, mockedGoCardlessEventService);
        when(mockedTransactionService.findTransaction(goCardlessPaymentFixture.getTransactionId())).thenReturn(transaction);
    }

    @Test
    public void handle_onPaidOutPaymentGoCardlessEvent_shouldSetAPayEventAsPaidOut() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("paid_out").toEntity());

        when(mockedGoCardlessEventService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.paymentPaidOutFor(transaction)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentPaidOutFor(transaction);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onCreatePaymentGoCardlessEvent_shouldSetAPayEventAsPaymentAcknowledged() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("created").toEntity());

        when(mockedGoCardlessEventService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.paymentAcknowledgedFor(transaction)).thenReturn(
                directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentAcknowledgedFor(transaction);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onPayoutPaidGoCardlessEvent_shouldSetRegisterPayoutPaidEvent() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("paid")
                .withResourceType(GoCardlessResourceType.PAYOUTS).toEntity());

        when(mockedGoCardlessEventService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.payoutPaidFor(transaction)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).payoutPaidFor(transaction);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onSubmittedPaymentGoCardlessEvent_shouldSetRegisterPaymentSubmittedEvent() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("submitted").toEntity());

        when(mockedGoCardlessEventService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.paymentSubmittedFor(transaction)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentSubmittedFor(transaction);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onConfirmedPaymentGoCardlessEvent_shouldLinkTheEventToAnExistingPaymentSubmittedPayEvent() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("confirmed").toEntity());

        when(mockedGoCardlessEventService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.findPaymentSubmittedEventFor(transaction)).thenReturn(Optional.of(
                directDebitEvent));

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onFailedPaymentGoCardlessEvent_shouldSetAPayEventAsFailed() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("failed").toEntity());

        when(mockedGoCardlessEventService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.paymentFailedWithEmailFor(transaction)).thenReturn(
                directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentFailedWithEmailFor(transaction);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onPayoutPaidGoCardlessEvent_shouldLinkTheEventToAnExistingPaymentPaidOutPayEvent() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("paid").toEntity());

        when(mockedGoCardlessEventService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.payoutPaidFor(transaction)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_doNotProcess_noOrgansiationOnGatewayAccount() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture
                .withOrganisationIdentifier(GoCardlessOrganisationId.valueOf("i_do_not_exist_id"))
                .withAction("paid")
                .toEntity());

        when(mockedGoCardlessEventService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());

        goCardlessPaymentHandler.handle(goCardlessEvent);
        verify(mockedGoCardlessEventService, never()).storeEvent(goCardlessEvent);
    }
}
