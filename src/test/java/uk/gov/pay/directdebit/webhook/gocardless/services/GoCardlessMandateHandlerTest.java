package uk.gov.pay.directdebit.webhook.gocardless.services;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateServiceFactory;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdateService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.services.DirectDebitEventService;
import uk.gov.pay.directdebit.payments.services.GoCardlessEventService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.MANDATE;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessMandateHandlerTest {

    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private MandateQueryService mockedMandateQueryService;
    @Mock
    private MandateStateUpdateService mockedMandateStateUpdateService;
    @Mock
    private MandateServiceFactory mockedMandateServiceFactory;
    @Mock
    private DirectDebitEventService mockedDirectDebitEventService;
    @Mock
    private GoCardlessEventService mockedGoCardlessService;

    @Captor
    private ArgumentCaptor<GoCardlessEvent> geCaptor;

    private DirectDebitEvent directDebitEvent = DirectDebitEventFixture.aDirectDebitEventFixture().toEntity();
    private PayerFixture payerFixture = PayerFixture.aPayerFixture();
    private GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_organisation");

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture()
            .withOrganisation(organisationIdentifier);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withGatewayAccountFixture(gatewayAccountFixture)
            .withPayerFixture(payerFixture);
    private GoCardlessMandateFixture goCardlessMandateFixture = GoCardlessMandateFixture.aGoCardlessMandateFixture()
            .withMandateId(mandateFixture.getId());
    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture()
            .withMandateFixture(mandateFixture);
    private GoCardlessMandateHandler goCardlessMandateHandler;
    private GoCardlessEventFixture goCardlessEventFixture = GoCardlessEventFixture.aGoCardlessEventFixture()
            .withOrganisationIdentifier(organisationIdentifier);

    @Before
    public void setUp() {
        goCardlessMandateHandler = new GoCardlessMandateHandler(mockedTransactionService, mockedGoCardlessService, mockedMandateServiceFactory, mockedDirectDebitEventService);
        when(mockedMandateServiceFactory.getMandateStateUpdateService()).thenReturn(mockedMandateStateUpdateService);
        when(mockedMandateServiceFactory.getMandateQueryService()).thenReturn(mockedMandateQueryService);        
        when(mockedTransactionService.findTransactionsForMandate(mandateFixture.getExternalId())).thenReturn(ImmutableList
                .of(transactionFixture.toEntity()));
        when(mockedMandateQueryService.findById(mandateFixture.getId())).thenReturn(mandateFixture.toEntity());
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldRegisterEventAsMandatePending_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("created").toEntity());

        when(mockedGoCardlessService.findGoCardlessMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedMandateStateUpdateService.mandatePendingFor(mandateFixture.toEntity())).thenReturn(
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
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("created").toEntity());

        when(mockedGoCardlessService.findGoCardlessMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedDirectDebitEventService.findBy(mandateFixture.getId(), MANDATE, SupportedEvent.MANDATE_PENDING)).thenReturn(Optional
                .of(
                directDebitEvent));
        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedMandateStateUpdateService, never()).mandatePendingFor(mandateFixture.toEntity());
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onActiveMandateGoCardlessEvent_shouldSetAPayEventAsMandateActive_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("active").toEntity());

        when(mockedGoCardlessService.findGoCardlessMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedMandateStateUpdateService.mandateActiveFor(mandateFixture.toEntity())).thenReturn(
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
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("active").toEntity());

        when(mockedGoCardlessService.findGoCardlessMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());

        when(mockedMandateStateUpdateService.mandateActiveFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedMandateStateUpdateService, never()).mandatePendingFor(mandateFixture.toEntity());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onSubmittedMandateGoCardlessEvent_shouldSetAPayEventAsMandatePending_whenDoesNotPreviouslyExist() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("submitted").toEntity());

        when(mockedGoCardlessService.findGoCardlessMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedMandateStateUpdateService.mandatePendingFor(mandateFixture.toEntity())).thenReturn(
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
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("submitted").toEntity());

        when(mockedGoCardlessService.findGoCardlessMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedDirectDebitEventService.findBy(mandateFixture.getId(), MANDATE, SupportedEvent.MANDATE_PENDING)).thenReturn(Optional.of(
                directDebitEvent));
        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedMandateStateUpdateService, never()).mandatePendingFor(mandateFixture.toEntity());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onAFailedMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateFailed() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("failed").toEntity());

        when(mockedGoCardlessService.findGoCardlessMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedMandateStateUpdateService.mandateFailedFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);
        when(mockedTransactionService.paymentFailedWithoutEmailFor(transactionFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentFailedWithoutEmailFor(transactionFixture.toEntity());
        verify(mockedMandateStateUpdateService).mandateFailedFor(mandateFixture.toEntity());
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onACancelledMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateCancelled_AndFailPaymentIfNotSubmittedToGC() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("cancelled").toEntity());

        when(mockedGoCardlessService.findGoCardlessMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findPaymentSubmittedEventFor(transactionFixture.toEntity())).thenReturn(Optional.empty());
        when(mockedMandateStateUpdateService.mandateCancelledFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);
        when(mockedTransactionService.paymentFailedWithoutEmailFor(transactionFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paymentFailedWithoutEmailFor(transactionFixture.toEntity());
        verify(mockedMandateStateUpdateService).mandateCancelledFor(mandateFixture.toEntity());
        verify(goCardlessEvent).setEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onACancelledMandateGoCardlessEvent_shouldRegisterAPayEventAsMandateCancelled_withoutFailingPaymentIfSubmittedToGC() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture.withAction("cancelled").toEntity());

        when(mockedGoCardlessService.findGoCardlessMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findPaymentSubmittedEventFor(transactionFixture.toEntity())).thenReturn(Optional.of(
                directDebitEvent));
        when(mockedMandateStateUpdateService.mandateCancelledFor(mandateFixture.toEntity())).thenReturn(
                directDebitEvent);

        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService, never()).paymentFailedWithoutEmailFor(transactionFixture.toEntity());
        verify(mockedMandateStateUpdateService).mandateCancelledFor(mandateFixture.toEntity());
        verify(mockedGoCardlessService).updateInternalEventId(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        assertThat(storedGoCardlessEvent.getEventId(), is(directDebitEvent.getId()));
        assertThat(storedGoCardlessEvent.getGoCardlessEventId(), is(goCardlessEvent.getGoCardlessEventId()));
    }

    @Test
    public void handle_onCreateMandateGoCardlessEvent_shouldNotRegisterEventAsMandatePending_whenOrganisationDoesNotExist() {
        GoCardlessEvent goCardlessEvent = spy(goCardlessEventFixture
                .withOrganisationIdentifier(GoCardlessOrganisationId.valueOf("does_not_exist"))
                .withAction("created")
                .toEntity());

        GoCardlessMandate goCardlessMandate = goCardlessMandateFixture.toEntity();
        when(mockedGoCardlessService.findGoCardlessMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandate);
        Mandate mockedMandate = MandateFixture.aMandateFixture()
                .withGatewayAccountFixture(GatewayAccountFixture.aGatewayAccountFixture()
                .withOrganisation(GoCardlessOrganisationId.valueOf("non_existing_organisation")))
                .toEntity();
        when(mockedMandateQueryService.findById(goCardlessMandate.getMandateId())).thenReturn(mockedMandate);

        goCardlessMandateHandler.handle(goCardlessEvent);
        verify(goCardlessEvent, never()).setEventId(anyLong());
        verify(mockedGoCardlessService, never()).storeEvent(goCardlessEvent);
    }
}
