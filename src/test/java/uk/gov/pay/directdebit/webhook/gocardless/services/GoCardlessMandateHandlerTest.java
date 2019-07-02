package uk.gov.pay.directdebit.webhook.gocardless.services;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.exception.GoCardlessEventHasNoMandateIdException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdateService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.services.DirectDebitEventService;
import uk.gov.pay.directdebit.payments.services.GoCardlessEventService;
import uk.gov.pay.directdebit.payments.services.PaymentService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.MANDATE;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessMandateHandlerTest {

    @Mock
    private PaymentService paymentService;
    @Mock
    private MandateQueryService mandateQueryService;
    @Mock
    private MandateStateUpdateService mandateStateUpdateService;
    @Mock
    private DirectDebitEventService directDebitEventService;
    @Mock
    private GoCardlessEventService goCardlessService;

    @Captor
    private ArgumentCaptor<GoCardlessEvent> geCaptor;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DirectDebitEvent directDebitEvent = DirectDebitEventFixture.aDirectDebitEventFixture().toEntity();
    private PayerFixture payerFixture = PayerFixture.aPayerFixture();
    private GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_organisation");

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
            .withOrganisation(organisationIdentifier);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withGatewayAccountFixture(gatewayAccountFixture)
            .withPayerFixture(payerFixture);
    private PaymentFixture paymentFixture = PaymentFixture.aPaymentFixture()
            .withMandateFixture(mandateFixture);
    private GoCardlessMandateHandler goCardlessMandateHandler;
    private GoCardlessEventFixture goCardlessEventFixture = GoCardlessEventFixture.aGoCardlessEventFixture()
            .withLinksOrganisation(organisationIdentifier);

    @Before
    public void setUp() {
        goCardlessMandateHandler = new GoCardlessMandateHandler(paymentService, goCardlessService, directDebitEventService, 
                mandateStateUpdateService, mandateQueryService);
        when(paymentService.findPaymentsForMandate(mandateFixture.getExternalId())).thenReturn(ImmutableList
                .of(paymentFixture.toEntity()));
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldRegisterEventAsMandatePending_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("created").toEntity());

        when(mandateQueryService.findByPaymentProviderMandateId(GOCARDLESS, goCardlessEvent.getLinksMandate().get(), goCardlessEventFixture.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());
        when(mandateStateUpdateService.mandatePendingFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);
        verify(goCardlessEvent).setInternalEventId(directDebitEvent.getId());
        verify(goCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getInternalEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldNotRegisterAnEventAsMandatePending_whenAPreviousMandatePendingEventExists() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("created").toEntity());

        when(mandateQueryService.findByPaymentProviderMandateId(GOCARDLESS, goCardlessEvent.getLinksMandate().get(), goCardlessEvent.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());
        when(directDebitEventService.findBy(mandateFixture.getId(), MANDATE, SupportedEvent.MANDATE_PENDING)).thenReturn(Optional
                .of(
                directDebitEvent));
        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mandateStateUpdateService, never()).mandatePendingFor(mandateFixture.toEntity());
        verify(goCardlessEvent).setInternalEventId(directDebitEvent.getId());
        verify(goCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getInternalEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onActiveMandateGoCardlessEvent_shouldSetAPayEventAsMandateActive_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("active").toEntity());

        when(mandateQueryService.findByPaymentProviderMandateId(GOCARDLESS, goCardlessEvent.getLinksMandate().get(), goCardlessEvent.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());
        when(mandateStateUpdateService.mandateActiveFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);
        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setInternalEventId(directDebitEvent.getId());
        verify(goCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getInternalEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onActiveMandateGoCardlessEvent_shouldNotRegisterAnEventAsMandatePending_whenAPreviousMandatePendingEventExists() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("active").toEntity());

        when(mandateQueryService.findByPaymentProviderMandateId(GOCARDLESS, goCardlessEvent.getLinksMandate().get(), goCardlessEvent.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());

        when(mandateStateUpdateService.mandateActiveFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setInternalEventId(directDebitEvent.getId());
        verify(mandateStateUpdateService, never()).mandatePendingFor(mandateFixture.toEntity());
        verify(goCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getInternalEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onSubmittedMandateGoCardlessEvent_shouldSetAPayEventAsMandatePending_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("submitted").toEntity());

        when(mandateQueryService.findByPaymentProviderMandateId(GOCARDLESS, goCardlessEvent.getLinksMandate().get(), goCardlessEvent.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());
        when(mandateStateUpdateService.mandatePendingFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setInternalEventId(directDebitEvent.getId());
        verify(goCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getInternalEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onSubmittedMandateGoCardlessEvent_shouldNotRegisterAnEventAsMandatePending_whenAPreviousMandatePendingEventExists() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("submitted").toEntity());

        when(mandateQueryService.findByPaymentProviderMandateId(GOCARDLESS, goCardlessEvent.getLinksMandate().get(), goCardlessEvent.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());
        when(directDebitEventService.findBy(mandateFixture.getId(), MANDATE, SupportedEvent.MANDATE_PENDING)).thenReturn(Optional.of(
                directDebitEvent));
        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setInternalEventId(directDebitEvent.getId());
        verify(mandateStateUpdateService, never()).mandatePendingFor(mandateFixture.toEntity());
        verify(goCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getInternalEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onAFailedMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateFailed() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("failed").toEntity());

        when(mandateQueryService.findByPaymentProviderMandateId(GOCARDLESS, goCardlessEvent.getLinksMandate().get(), goCardlessEvent.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());
        when(mandateStateUpdateService.mandateFailedFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);
        when(paymentService.paymentFailedWithoutEmailFor(paymentFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(paymentService).paymentFailedWithoutEmailFor(paymentFixture.toEntity());
        verify(mandateStateUpdateService).mandateFailedFor(mandateFixture.toEntity());
        verify(goCardlessEvent).setInternalEventId(directDebitEvent.getId());
        verify(goCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getInternalEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onACancelledMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateCancelled_AndFailPaymentIfNotSubmittedToGC() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("cancelled").toEntity());

        when(mandateQueryService.findByPaymentProviderMandateId(GOCARDLESS, goCardlessEvent.getLinksMandate().get(), goCardlessEvent.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());
        when(paymentService.findPaymentSubmittedEventFor(paymentFixture.toEntity())).thenReturn(Optional.empty());
        when(mandateStateUpdateService.mandateCancelledFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);
        when(paymentService.paymentFailedWithoutEmailFor(paymentFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(paymentService).paymentFailedWithoutEmailFor(paymentFixture.toEntity());
        verify(mandateStateUpdateService).mandateCancelledFor(mandateFixture.toEntity());
        verify(goCardlessEvent).setInternalEventId(directDebitEvent.getId());
        verify(goCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getInternalEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onACancelledMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateCancelled_withoutFailingPaymentIfSubmittedToGC() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("cancelled").toEntity());

        when(mandateQueryService.findByPaymentProviderMandateId(GOCARDLESS, goCardlessEvent.getLinksMandate().get(), goCardlessEvent.getLinksOrganisation()))
                .thenReturn(mandateFixture.toEntity());
        when(paymentService.findPaymentSubmittedEventFor(paymentFixture.toEntity())).thenReturn(Optional.of(
                directDebitEvent));
        when(mandateStateUpdateService.mandateCancelledFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(paymentService, never()).paymentFailedWithoutEmailFor(paymentFixture.toEntity());
        verify(mandateStateUpdateService).mandateCancelledFor(mandateFixture.toEntity());
        verify(goCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getInternalEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldNotRegisterEventAsMandatePending_whenOrganisationDoesNotExist() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("does_not_exist"))
                .withAction("created")
                .toEntity());

        when(mandateQueryService.findByPaymentProviderMandateId(GOCARDLESS, goCardlessEvent.getLinksMandate().get(),
                goCardlessEventFixture.getLinksOrganisation())).thenReturn(mandateFixture.toEntity());

        goCardlessMandateHandler.handle(goCardlessEvent);
        verify(goCardlessEvent, never()).setInternalEventId(anyLong());
        verify(goCardlessService, never()).storeEvent(goCardlessEvent);
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldThrowExceptionWhenEventHasNoLinkedMandate() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("does_not_exist"))
                .withAction("created")
                .withLinksMandate(null)
                .toEntity());
        
        thrown.expect(GoCardlessEventHasNoMandateIdException.class);
        goCardlessMandateHandler.handle(goCardlessEvent);
    }
}
