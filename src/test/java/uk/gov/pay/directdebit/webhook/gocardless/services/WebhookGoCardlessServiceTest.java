package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotFoundException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessPaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.services.GoCardlessEventService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.UNHANDLED;

@RunWith(MockitoJUnitRunner.class)

public class WebhookGoCardlessServiceTest {
    @Mock
    private GoCardlessEventService mockedGoCardlessEventService;
    @Mock
    private GoCardlessPaymentHandler mockedGoCardlessPaymentHandler;
    @Mock
    private GoCardlessMandateHandler mockedGoCardlessMandateHandler;

    private WebhookGoCardlessService webhookGoCardlessService;

    @Before
    public void setUp() {
        webhookGoCardlessService = new WebhookGoCardlessService(mockedGoCardlessEventService, mockedGoCardlessPaymentHandler, mockedGoCardlessMandateHandler);
    }

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
        } catch (GoCardlessPaymentNotFoundException expected) { }
        verify(mockedGoCardlessEventService).storeEvent(goCardlessEvent);
    }

    @Test
    public void shouldStoreMandateEventsWhenHandlingThemThrowsAnException() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(MANDATES).withAction("created").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);

        doThrow(new GoCardlessMandateNotFoundException("error", "OOPSIE")).when(mockedGoCardlessMandateHandler).handle(goCardlessEvent);
        try {
            webhookGoCardlessService.handleEvents(events);
            fail("Expected GoCardlessMandateNotFoundException.");
        } catch (GoCardlessMandateNotFoundException expected) { }
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
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessEventService).storeEvent(goCardlessEvent);
    }
}
