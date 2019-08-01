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
    public void shouldLogForUnhandledMandateActions() {
        GoCardlessEvent unhandledMandateEvent1 = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withAction(ACTION_MANDATE_REPLACED)
                .withLinksMandate(GoCardlessMandateId.valueOf("test-mandate-id1"))
                .toEntity();

        GoCardlessEvent unhandledMandateEvent2 = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withAction(ACTION_MANDATE_RESUBMISSION_REQUESTED)
                .withLinksMandate(GoCardlessMandateId.valueOf("test-mandate-id2"))
                .toEntity();

        GoCardlessEvent handledMandateEvent = aGoCardlessEventFixture()
                .withResourceType(MANDATES)
                .withAction(ACTION_MANDATE_ACTIVE)
                .withLinksMandate(GoCardlessMandateId.valueOf("test-mandate-id3"))
                .toEntity();

        GoCardlessEvent unhandledPaymentEvent = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
                .withAction(ACTION_PAYMENT_RESUBMISSION_REQUESTED)
                .withLinksPayment(GoCardlessPaymentId.valueOf("test-payment-id1"))
                .toEntity();
        
        GoCardlessEvent handledPaymentEvent = aGoCardlessEventFixture()
                .withResourceType(PAYMENTS)
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
                "Received a GoCardless event with action replaced for mandate test-mandate-id1, which we do not expect to receive and do not handle",
                "Received a GoCardless event with action resubmission_requested for mandate test-mandate-id2, which we do not expect to receive and do not handle",
                "Received a GoCardless event with action resubmission_requested for payment test-payment-id1, which we do not expect to receive and do not handle"));
    }
    
    private List<String> getLoggedMessages() {
        return loggingEventArgumentCaptor
                    .getAllValues()
                    .stream()
                    .map(LoggingEvent::getFormattedMessage)
                    .collect(Collectors.toList());
    }
}
