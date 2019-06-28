package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.exception.EventHasNoPaymentIdException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.exception.PaymentNotFoundException;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.GoCardlessEventService;
import uk.gov.pay.directdebit.payments.services.PaymentService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessPaymentHandlerTest {

    @Mock
    private PaymentService mockedPaymentService;
    @Mock
    private GoCardlessEventService mockedGoCardlessEventService;
    @Captor
    private ArgumentCaptor<GoCardlessEvent> geCaptor;

    private GoCardlessPaymentHandler goCardlessPaymentHandler;
    private DirectDebitEvent directDebitEvent = DirectDebitEventFixture.aDirectDebitEventFixture().toEntity();
    private GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_org_id");
    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().withOrganisation(organisationIdentifier);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);
    private Payment payment = PaymentFixture.aPaymentFixture().withMandateFixture(mandateFixture).toEntity();
    private GoCardlessEventFixture goCardlessEventFixture = GoCardlessEventFixture.aGoCardlessEventFixture().withLinksOrganisation(organisationIdentifier);

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Before
    public void setUp() {
        goCardlessPaymentHandler = new GoCardlessPaymentHandler(mockedPaymentService, mockedGoCardlessEventService);
    }

    @Test
    public void handle_onPaidOutPaymentGoCardlessEvent_shouldSetAPayEventAsPaidOut() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("paid_out").toEntity());

        when(mockedPaymentService.findPaymentByProviderId(GOCARDLESS, goCardlessEvent.getLinksPayment().get()))
                .thenReturn(Optional.of(payment));
        when(mockedPaymentService.paymentPaidOutFor(payment)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedPaymentService).paymentPaidOutFor(payment);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onCreatePaymentGoCardlessEvent_shouldSetAPayEventAsPaymentAcknowledged() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("created").toEntity());

        when(mockedPaymentService.findPaymentByProviderId(GOCARDLESS, goCardlessEvent.getLinksPayment().get()))
                .thenReturn(Optional.of(payment));
        when(mockedPaymentService.paymentAcknowledgedFor(payment)).thenReturn(
                directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedPaymentService).paymentAcknowledgedFor(payment);
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

        when(mockedPaymentService.findPaymentByProviderId(GOCARDLESS, goCardlessEvent.getLinksPayment().get()))
                .thenReturn(Optional.of(payment));
        when(mockedPaymentService.payoutPaidFor(payment)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedPaymentService).payoutPaidFor(payment);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onSubmittedPaymentGoCardlessEvent_shouldSetRegisterPaymentSubmittedEvent() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("submitted").toEntity());

        when(mockedPaymentService.findPaymentByProviderId(GOCARDLESS, goCardlessEvent.getLinksPayment().get()))
                .thenReturn(Optional.of(payment));
        when(mockedPaymentService.paymentSubmittedFor(payment)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedPaymentService).paymentSubmittedFor(payment);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onConfirmedPaymentGoCardlessEvent_shouldLinkTheEventToAnExistingPaymentSubmittedPayEvent() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("confirmed").toEntity());

        when(mockedPaymentService.findPaymentByProviderId(GOCARDLESS, goCardlessEvent.getLinksPayment().get()))
                .thenReturn(Optional.of(payment));
        when(mockedPaymentService.findPaymentSubmittedEventFor(payment)).thenReturn(Optional.of(
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

        when(mockedPaymentService.findPaymentByProviderId(GOCARDLESS, goCardlessEvent.getLinksPayment().get()))
                .thenReturn(Optional.of(payment));
        when(mockedPaymentService.paymentFailedWithEmailFor(payment)).thenReturn(
                directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedPaymentService).paymentFailedWithEmailFor(payment);
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onPayoutPaidGoCardlessEvent_shouldLinkTheEventToAnExistingPaymentPaidOutPayEvent() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("paid").toEntity());

        when(mockedPaymentService.findPaymentByProviderId(GOCARDLESS, goCardlessEvent.getLinksPayment().get()))
                .thenReturn(Optional.of(payment));
        when(mockedPaymentService.payoutPaidFor(payment)).thenReturn(directDebitEvent);

        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessEventService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        Assert.assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_doNotProcess_organisationOnEventDoesNotMatchGatewayAccount() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("i_do_not_exist_id"))
                .withAction("paid")
                .toEntity());

        when(mockedPaymentService.findPaymentByProviderId(GOCARDLESS, goCardlessEvent.getLinksPayment().get()))
                .thenReturn(Optional.of(payment));

        goCardlessPaymentHandler.handle(goCardlessEvent);
        verify(mockedGoCardlessEventService, never()).storeEvent(goCardlessEvent);
    }

    @Test
    public void handle_onCreatePaymentGoCardlessEvent_shouldThrowExceptionWhenEventHasNoLinkedPayment() {
        GoCardlessEvent goCardlessEvent = goCardlessEventFixture
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("does_not_exist"))
                .withAction("created")
                .withLinksPayment(null)
                .toEntity();

        thrown.expect(EventHasNoPaymentIdException.class);
        goCardlessPaymentHandler.handle(goCardlessEvent);
    }

    @Test
    public void handle_onCreatePaymentGoCardlessEvent_shouldThrowPaymentNotFoundException() {
        GoCardlessPaymentId expectedPaymentProvider = GoCardlessPaymentId.valueOf("expected");
        GoCardlessEvent goCardlessEvent = goCardlessEventFixture
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("does_not_exist"))
                .withAction("created")
                .withLinksPayment(expectedPaymentProvider)
                .toEntity();
        
        when(mockedPaymentService.findPaymentByProviderId(GOCARDLESS, expectedPaymentProvider)).thenReturn(Optional.empty());

        thrown.expect(PaymentNotFoundException.class);
        goCardlessPaymentHandler.handle(goCardlessEvent);
    }
}
