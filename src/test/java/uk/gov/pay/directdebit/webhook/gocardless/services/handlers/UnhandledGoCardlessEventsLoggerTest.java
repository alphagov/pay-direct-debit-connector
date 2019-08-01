package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GoCardlessEventId;
import uk.gov.pay.directdebit.events.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_REPLACED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_MANDATE_RESUBMISSION_REQUESTED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_FAILED;
import static uk.gov.pay.directdebit.events.model.GoCardlessEvent.ACTION_PAYMENT_RESUBMISSION_REQUESTED;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;

@RunWith(MockitoJUnitRunner.class)
public class UnhandledGoCardlessEventsLoggerTest {
    
    @Mock
    private Appender<ILoggingEvent> mockAppender;
    
    @InjectMocks
    private UnhandledGoCardlessEventsLogger unhandledGoCardlessEventsLogger;

    @Captor
    ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor;
    
    @Before
    public void setUp() {
        Logger root = (Logger) LoggerFactory.getLogger(UnhandledGoCardlessEventsLogger.class);
        root.addAppender(mockAppender);
    }

    @Test
    public void shouldLogForUnhandledEventActions() {
        GoCardlessEvent unhandledMandateEvent1 = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withGoCardlessEventId(GoCardlessEventId.valueOf("event1"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("ORG1"))
                .withAction(ACTION_MANDATE_REPLACED)
                .withLinksMandate(GoCardlessMandateId.valueOf("test-mandate-id1"))
                .toEntity();

        GoCardlessEvent unhandledMandateEvent2 = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withGoCardlessEventId(GoCardlessEventId.valueOf("event2"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("ORG1"))
                .withAction(ACTION_MANDATE_RESUBMISSION_REQUESTED)
                .withLinksMandate(GoCardlessMandateId.valueOf("test-mandate-id2"))
                .toEntity();

        GoCardlessEvent handledMandateEvent = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withGoCardlessEventId(GoCardlessEventId.valueOf("event3"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("ORG1"))
                .withAction(ACTION_MANDATE_ACTIVE)
                .withLinksMandate(GoCardlessMandateId.valueOf("test-mandate-id3"))
                .toEntity();

        GoCardlessEvent unhandledPaymentEvent = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
                .withGoCardlessEventId(GoCardlessEventId.valueOf("event4"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("ORG1"))
                .withAction(ACTION_PAYMENT_RESUBMISSION_REQUESTED)
                .withLinksPayment(GoCardlessPaymentId.valueOf("test-payment-id1"))
                .toEntity();
        
        GoCardlessEvent handledPaymentEvent = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
                .withGoCardlessEventId(GoCardlessEventId.valueOf("event5"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("ORG1"))
                .withAction(ACTION_PAYMENT_FAILED)
                .withLinksPayment(GoCardlessPaymentId.valueOf("test-payment-id2"))
                .toEntity();

        unhandledGoCardlessEventsLogger.logUnhandledEvents(List.of(
                unhandledMandateEvent1,
                unhandledMandateEvent2,
                handledMandateEvent,
                unhandledPaymentEvent,
                handledPaymentEvent));
        
        verify(mockAppender, times(3)).doAppend(loggingEventArgumentCaptor.capture());

        List<String> loggedMessages = getLoggedMessages();

        assertThat(loggedMessages, containsInAnyOrder(
                "Received a GoCardless event with id event1 for organisation ORG1 with action replaced for mandate test-mandate-id1, which we do not expect to receive and do not handle.",
                "Received a GoCardless event with id event2 for organisation ORG1 with action resubmission_requested for mandate test-mandate-id2, which we do not expect to receive and do not handle.",
                "Received a GoCardless event with id event4 for organisation ORG1 with action resubmission_requested for payment test-payment-id1, which we do not expect to receive and do not handle."));
    }

    @Test
    public void shouldLogForUnhandledResourceTypes() {
        GoCardlessEvent subscriptionsEvent = aGoCardlessEventFixture()
                .withResourceType(GoCardlessResourceType.SUBSCRIPTIONS)
                .withGoCardlessEventId(GoCardlessEventId.valueOf("event1"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("ORG1"))
                .toEntity();

        GoCardlessEvent refundsEvent = aGoCardlessEventFixture()
                .withResourceType(GoCardlessResourceType.REFUNDS)
                .withGoCardlessEventId(GoCardlessEventId.valueOf("event2"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("ORG1"))
                .toEntity();
        
        GoCardlessEvent payoutsEvent = aGoCardlessEventFixture()
                .withResourceType(GoCardlessResourceType.PAYOUTS)
                .withGoCardlessEventId(GoCardlessEventId.valueOf("event3"))
                .withLinksOrganisation(GoCardlessOrganisationId.valueOf("ORG1"))
                .toEntity();

        unhandledGoCardlessEventsLogger.logUnhandledEvents(List.of(subscriptionsEvent, refundsEvent, payoutsEvent));
        
        verify(mockAppender, times(2)).doAppend(loggingEventArgumentCaptor.capture());

        List<String> loggedMessages = getLoggedMessages();

        assertThat(loggedMessages, containsInAnyOrder(
                "Received a GoCardless event with id event1 for organisation ORG1 with resource type SUBSCRIPTIONS, which we do not expect to receive and do not handle.",
                "Received a GoCardless event with id event2 for organisation ORG1 with resource type REFUNDS, which we do not expect to receive and do not handle."));
    }

    private List<String> getLoggedMessages() {
        return loggingEventArgumentCaptor
                    .getAllValues()
                    .stream()
                    .map(LoggingEvent::getFormattedMessage)
                    .collect(Collectors.toUnmodifiableList());
    }
}
