package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.services.GoCardlessEventService;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessMandateStateUpdater;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotFoundException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessPaymentNotFoundException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdAndOrganisationId;
import uk.gov.pay.directdebit.payments.services.gocardless.GoCardlessPaymentStateUpdater;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.UNHANDLED;

@RunWith(MockitoJUnitRunner.class)

public class WebhookGoCardlessServiceTest {
    @Mock
    private GoCardlessEventService mockedGoCardlessEventService;

    @Mock
    private GoCardlessPaymentHandler mockedGoCardlessPaymentHandler;

    @Mock
    private GoCardlessMandateHandler mockedGoCardlessMandateHandler;

    @Mock
    private GoCardlessMandateStateUpdater mockedGoCardlessMandateStateUpdater;

    @Mock
    private GoCardlessPaymentStateUpdater mockedGoCardlessPaymentStateUpdater;

    @Mock
    private MandateQueryService mockedMandateQueryService;

    @InjectMocks
    private WebhookGoCardlessService webhookGoCardlessService;

    @Test
    public void shouldInsertEvents() {
        GoCardlessEvent firstGoCardlessEvent = aGoCardlessEventFixture().toEntity();
        GoCardlessEvent secondGoCardlessEvent = aGoCardlessEventFixture().toEntity();
        List<GoCardlessEvent> events = Arrays.asList(firstGoCardlessEvent, secondGoCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventService).storeEvent(firstGoCardlessEvent);
        verify(mockedGoCardlessEventService).storeEvent(secondGoCardlessEvent);
    }

    @Test
    public void shouldStoreEventsWithAnUnhandledResource() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(UNHANDLED).withAction("created").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventService).storeEvent(goCardlessEvent);
    }

    @Test
    public void shouldStoreButNotHandlePaymentEventsWithAnUnhandledAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(PAYMENTS).withAction("not_handled").toEntity();
        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);

        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventService).storeEvent(goCardlessEvent);
    }

    @Test
    public void shouldStoreButNotHandleMandateEventsWithAnUnhandledAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(MANDATES).withAction("not_handled_again").toEntity();
        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);

        when(mockedMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                goCardlessEvent.getLinksMandate().get(), goCardlessEvent.getLinksOrganisation()))
                .thenReturn(mock(Mandate.class));

        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventService).storeEvent(goCardlessEvent);
    }

    @Test
    public void shouldStorePaymentEventsWhenHandlingThemThrowsAnException() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(PAYMENTS).withAction("created").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);

        doThrow(new GoCardlessPaymentNotFoundException("OOPSIE")).when(mockedGoCardlessPaymentHandler).handle(goCardlessEvent);
        try {
            webhookGoCardlessService.handleEvents(events);
            fail("Expected GoCardlessPaymentNotFoundException.");
        } catch (GoCardlessPaymentNotFoundException expected) {
        }
        verify(mockedGoCardlessEventService).storeEvent(goCardlessEvent);
    }

    @Test
    public void shouldStoreMandateEventsWhenHandlingThemThrowsAnException() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(MANDATES).withAction("created").toEntity();
        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);

        when(mockedMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                goCardlessEvent.getLinksMandate().get(), goCardlessEvent.getLinksOrganisation()))
                .thenReturn(mock(Mandate.class));

        doThrow(new GoCardlessMandateNotFoundException("error", "OOPSIE")).when(mockedGoCardlessMandateHandler).handle(goCardlessEvent);
        try {
            webhookGoCardlessService.handleEvents(events);
            fail("Expected GoCardlessMandateNotFoundException.");
        } catch (GoCardlessMandateNotFoundException expected) {
        }
        verify(mockedGoCardlessEventService).storeEvent(goCardlessEvent);
    }

    @Test
    public void shouldStorePaymentEventsWithAValidAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(PAYMENTS).withAction("paid_out").toEntity();
        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);

        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventService).storeEvent(goCardlessEvent);
    }

    @Test
    public void shouldStoreMandateEventsWithAValidAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(MANDATES).withAction("created").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);

        when(mockedMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                goCardlessEvent.getLinksMandate().get(), goCardlessEvent.getLinksOrganisation()))
                .thenReturn(mock(Mandate.class));

        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventService).storeEvent(goCardlessEvent);
    }

    @Test
    public void shouldUpdateStatesForMandatesAndPaymentsAffectedByEvents() {
        GoCardlessOrganisationId goCardlessOrganisationId1 = GoCardlessOrganisationId.valueOf("OR1");
        GoCardlessOrganisationId goCardlessOrganisationId2 = GoCardlessOrganisationId.valueOf("OR2");

        GoCardlessMandateId goCardlessMandateId1 = GoCardlessMandateId.valueOf("MD1");
        GoCardlessMandateId goCardlessMandateId2 = GoCardlessMandateId.valueOf("MD2");

        GoCardlessPaymentId goCardlessPaymentId1 = GoCardlessPaymentId.valueOf("PM1");
        GoCardlessPaymentId goCardlessPaymentId2 = GoCardlessPaymentId.valueOf("PM2");

        GoCardlessEvent goCardlessOrganisation1Mandate1Event = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withLinksMandate(goCardlessMandateId1)
                .withLinksOrganisation(goCardlessOrganisationId1)
                .toEntity();

        GoCardlessEvent anotherGoCardlessOrganisation1Mandate1Event = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withLinksMandate(goCardlessMandateId1)
                .withLinksOrganisation(goCardlessOrganisationId1)
                .toEntity();

        GoCardlessEvent goCardlessOrganisation1Mandate2Event = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withLinksMandate(goCardlessMandateId2)
                .withLinksOrganisation(goCardlessOrganisationId1)
                .toEntity();

        GoCardlessEvent goCardlessOrganisation1Payment1Event = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
                .withLinksPayment(goCardlessPaymentId1)
                .withLinksOrganisation(goCardlessOrganisationId1)
                .toEntity();

        GoCardlessEvent goCardlessOrganisation1Payment2Event = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
                .withLinksPayment(goCardlessPaymentId2)
                .withLinksOrganisation(goCardlessOrganisationId1)
                .toEntity();

        GoCardlessEvent goCardlessOrganisation2Mandate1Event = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withLinksMandate(goCardlessMandateId1)
                .withLinksOrganisation(goCardlessOrganisationId2)
                .toEntity();

        Mandate mandate1 = mock(Mandate.class);
        Mandate mandate2 = mock(Mandate.class);
        Mandate mandate3 = mock(Mandate.class);

        when(mockedMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(goCardlessMandateId1, goCardlessOrganisationId1))
                .thenReturn(mandate1);

        when(mockedMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(goCardlessMandateId2, goCardlessOrganisationId1))
                .thenReturn(mandate2);

        when(mockedMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(goCardlessMandateId1, goCardlessOrganisationId2))
                .thenReturn(mandate3);

        webhookGoCardlessService.handleEvents(List.of(
                goCardlessOrganisation1Mandate1Event,
                goCardlessOrganisation1Mandate2Event,
                anotherGoCardlessOrganisation1Mandate1Event,
                goCardlessOrganisation1Payment1Event,
                goCardlessOrganisation1Payment2Event,
                goCardlessOrganisation2Mandate1Event));

        verify(mockedGoCardlessMandateStateUpdater).updateState(mandate1);
        verify(mockedGoCardlessMandateStateUpdater).updateState(mandate2);
        verify(mockedGoCardlessMandateStateUpdater).updateState(mandate3);
        verify(mockedGoCardlessPaymentStateUpdater).updateState(new GoCardlessPaymentIdAndOrganisationId(goCardlessPaymentId1, goCardlessOrganisationId1));
        verify(mockedGoCardlessPaymentStateUpdater).updateState(new GoCardlessPaymentIdAndOrganisationId(goCardlessPaymentId2, goCardlessOrganisationId1));
    }

    @Test
    public void shouldNotBreakHorriblyIfAMandateEventIsNotLinkedToAMandateOrAPaymentEventIsNotLinkedToAPayment() {
        GoCardlessEvent legitimateMandateEvent = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withLinksMandate(GoCardlessMandateId.valueOf("MD123"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("OR123"))
                .toEntity();

        GoCardlessEvent legitimatePaymentEvent = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
                .withLinksPayment(GoCardlessPaymentId.valueOf("PM123"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("OR123"))
                .toEntity();

        GoCardlessEvent cursedMandateEventNotLinkedToMandate = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withLinksMandate(null)
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("OR123"))
                .toEntity();

        GoCardlessEvent cursedPaymentEventNotLinkedToPayment = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
                .withLinksPayment(null)
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("OR123"))
                .toEntity();

        Mandate mandate = mock(Mandate.class);

        when(mockedMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                GoCardlessMandateId.valueOf("MD123"), GoCardlessOrganisationId.valueOf("OR123"))).thenReturn(mandate);

        webhookGoCardlessService.handleEvents(List.of(
                legitimateMandateEvent,
                legitimatePaymentEvent,
                cursedMandateEventNotLinkedToMandate,
                cursedPaymentEventNotLinkedToPayment));

        verify(mockedGoCardlessMandateStateUpdater).updateState(mandate);
        verify(mockedGoCardlessPaymentStateUpdater).updateState(new GoCardlessPaymentIdAndOrganisationId(GoCardlessPaymentId.valueOf("PM123"),
                GoCardlessOrganisationId.valueOf("OR123")));
    }

}
