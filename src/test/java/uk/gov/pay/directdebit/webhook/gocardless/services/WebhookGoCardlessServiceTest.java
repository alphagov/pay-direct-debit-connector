package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.events.services.GoCardlessEventService;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdater;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotFoundException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessPaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.payments.services.PaymentStateUpdater;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.SendEmailsForGoCardlessEventsHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;

@RunWith(MockitoJUnitRunner.class)
public class WebhookGoCardlessServiceTest {
    @Mock
    private GoCardlessEventService mockedGoCardlessEventService;

    @Mock
    private SendEmailsForGoCardlessEventsHandler mockedSendEmailsForGoCardlessEventsHandler;

    @Mock
    private MandateStateUpdater mockedMandateStateUpdater;

    @Mock
    private PaymentStateUpdater mockedPaymentStateUpdater;

    @Mock
    private MandateQueryService mockedMandateQueryService;
    
    @Mock
    private PaymentQueryService mockedPaymentQueryService;

    @InjectMocks
    private WebhookGoCardlessService webhookGoCardlessService;

    @Test
    public void shouldInsertEvents() {
        Arrays.stream(GoCardlessResourceType.values()).forEach(t -> {
                    GoCardlessEvent event = aGoCardlessEventFixture().withResourceType(t).toEntity();

                    when(mockedMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                            event.getLinksMandate().get(), event.getLinksOrganisation()))
                            .thenReturn(mock(Mandate.class));

                    webhookGoCardlessService.processEvents(List.of(event));
                    verify(mockedGoCardlessEventService).storeEvent(event);
                }
        );
    }
    
    @Test
    public void shouldStorePaymentEventsWhenHandlingThemThrowsAnException() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(PAYMENTS).withAction("created").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);

        doThrow(new GoCardlessPaymentNotFoundException("OOPSIE"))
                .when(mockedSendEmailsForGoCardlessEventsHandler).sendEmails(List.of(goCardlessEvent));
        try {
            webhookGoCardlessService.processEvents(events);
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

        doThrow(new GoCardlessMandateNotFoundException("error", "OOPSIE"))
                .when(mockedSendEmailsForGoCardlessEventsHandler).sendEmails(List.of(goCardlessEvent));
        try {
            webhookGoCardlessService.processEvents(events);
            fail("Expected GoCardlessMandateNotFoundException.");
        } catch (GoCardlessMandateNotFoundException expected) {
        }
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
                .withAction("active")
                .withLinksMandate(goCardlessMandateId1)
                .withLinksOrganisation(goCardlessOrganisationId1)
                .toEntity();

        GoCardlessEvent anotherGoCardlessOrganisation1Mandate1Event = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withAction("active")
                .withLinksMandate(goCardlessMandateId1)
                .withLinksOrganisation(goCardlessOrganisationId1)
                .toEntity();

        GoCardlessEvent goCardlessOrganisation1Mandate2Event = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withAction("active")
                .withLinksMandate(goCardlessMandateId2)
                .withLinksOrganisation(goCardlessOrganisationId1)
                .toEntity();

        GoCardlessEvent goCardlessOrganisation1Payment1Event = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
                .withAction("confirmed")
                .withLinksPayment(goCardlessPaymentId1)
                .withLinksOrganisation(goCardlessOrganisationId1)
                .toEntity();

        GoCardlessEvent goCardlessOrganisation1Payment2Event = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
                .withAction("confirmed")
                .withLinksPayment(goCardlessPaymentId2)
                .withLinksOrganisation(goCardlessOrganisationId1)
                .toEntity();

        GoCardlessEvent goCardlessOrganisation2Mandate1Event = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withAction("active")
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

        Payment payment1 = mock(Payment.class);
        Payment payment2 = mock(Payment.class);
        
        when(mockedPaymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessPaymentId1, goCardlessOrganisationId1))
                .thenReturn(Optional.of(payment1));

        when(mockedPaymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessPaymentId2, goCardlessOrganisationId1))
                .thenReturn(Optional.of(payment2));

        webhookGoCardlessService.processEvents(List.of(
                goCardlessOrganisation1Mandate1Event,
                goCardlessOrganisation1Mandate2Event,
                anotherGoCardlessOrganisation1Mandate1Event,
                goCardlessOrganisation1Payment1Event,
                goCardlessOrganisation1Payment2Event,
                goCardlessOrganisation2Mandate1Event));

        verify(mockedMandateStateUpdater).updateStateIfNecessary(mandate1);
        verify(mockedMandateStateUpdater).updateStateIfNecessary(mandate2);
        verify(mockedMandateStateUpdater).updateStateIfNecessary(mandate3);
        verify(mockedPaymentStateUpdater).updateStateIfNecessary(payment1);
        verify(mockedPaymentStateUpdater).updateStateIfNecessary(payment2);
    }

    @Test
    public void shouldNotBreakHorriblyIfAMandateEventIsNotLinkedToAMandateOrAPaymentEventIsNotLinkedToAPayment() {
        GoCardlessMandateId mandateId = GoCardlessMandateId.valueOf("MD123");
        GoCardlessPaymentId paymentId = GoCardlessPaymentId.valueOf("PM123");
        GoCardlessOrganisationId organisationId = GoCardlessOrganisationId.valueOf("OR123");

        GoCardlessEvent legitimateMandateEvent = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withAction("active")
                .withLinksMandate(mandateId)
                .withLinksOrganisation(organisationId)
                .toEntity();

        GoCardlessEvent legitimatePaymentEvent = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
                .withAction("confirmed")
                .withLinksPayment(paymentId)
                .withLinksOrganisation(organisationId)
                .toEntity();

        GoCardlessEvent cursedMandateEventNotLinkedToMandate = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withAction("active")
                .withLinksMandate(null)
                .withLinksOrganisation(organisationId)
                .toEntity();

        GoCardlessEvent cursedPaymentEventNotLinkedToPayment = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
                .withAction("confirmed")
                .withLinksPayment(null)
                .withLinksOrganisation(organisationId)
                .toEntity();

        Mandate mandate = mock(Mandate.class);

        when(mockedMandateQueryService.findByGoCardlessMandateIdAndOrganisationId(mandateId, organisationId))
                .thenReturn(mandate);
        
        Payment payment = mock(Payment.class);
        
        when(mockedPaymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(paymentId, organisationId))
                .thenReturn(Optional.of(payment));

        webhookGoCardlessService.processEvents(List.of(
                legitimateMandateEvent,
                legitimatePaymentEvent,
                cursedMandateEventNotLinkedToMandate,
                cursedPaymentEventNotLinkedToPayment));

        verify(mockedMandateStateUpdater).updateStateIfNecessary(mandate);
        verify(mockedPaymentStateUpdater).updateStateIfNecessary(payment);
    }

}
