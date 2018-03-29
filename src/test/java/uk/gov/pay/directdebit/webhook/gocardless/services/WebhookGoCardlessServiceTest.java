package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotFoundException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessPaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture.aGoCardlessMandateFixture;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture.aGoCardlessPaymentFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.UNHANDLED;

@RunWith(MockitoJUnitRunner.class)

public class WebhookGoCardlessServiceTest {
    @Mock
    private GoCardlessService mockedGoCardlessService;
    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private PayerService mockedPayerService;
    @Mock
    private GoCardlessPaymentHandler mockedGoCardlessPaymentHandler;
    @Mock
    private GoCardlessMandateHandler mockedGoCardlessMandateHandler;

    private WebhookGoCardlessService webhookGoCardlessService;

    @Before
    public void setUp() {
        webhookGoCardlessService = new WebhookGoCardlessService(mockedGoCardlessService, mockedTransactionService, mockedPayerService, mockedGoCardlessPaymentHandler, mockedGoCardlessMandateHandler);
    }

    @Test
    public void shouldInsertEvents() {
        GoCardlessEvent firstGoCardlessEvent = aGoCardlessEventFixture().toEntity();
        GoCardlessEvent secondGoCardlessEvent = aGoCardlessEventFixture().toEntity();
        List<GoCardlessEvent> events = Arrays.asList(firstGoCardlessEvent, secondGoCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(firstGoCardlessEvent);
        verify(mockedGoCardlessService).storeEvent(secondGoCardlessEvent);
    }

    @Test
    public void shouldStoreEventsWithAnUnhandledResource() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(UNHANDLED).withAction("created").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
        verifyZeroInteractions(mockedTransactionService);
    }

    @Test
    public void shouldStoreButNotHandlePaymentEventsWithAnUnhandledAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(PAYMENTS).withAction("not_handled").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
        verifyZeroInteractions(mockedTransactionService);
    }

    @Test
    public void shouldStoreButNotHandleMandateEventsWithAnUnhandledAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(MANDATES).withAction("not_handled_again").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
        verifyZeroInteractions(mockedTransactionService);
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
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
        verifyZeroInteractions(mockedTransactionService);
    }

    @Test
    public void shouldStoreMandateEventsWhenHandlingThemThrowsAnException() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(MANDATES).withAction("created").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);

        doThrow(new GoCardlessMandateNotFoundException("OOPSIE")).when(mockedGoCardlessMandateHandler).handle(goCardlessEvent);
        try {
            webhookGoCardlessService.handleEvents(events);
            fail("Expected GoCardlessMandateNotFoundException.");
        } catch (GoCardlessMandateNotFoundException expected) { }
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
        verifyZeroInteractions(mockedTransactionService);
    }

    @Test
    public void shouldStorePaymentEventsWithAValidAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(PAYMENTS).withAction("paid_out").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
    }

    @Test
    public void shouldStoreMandateEventsWithAValidAction() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withResourceType(MANDATES).withAction("created").toEntity();

        List<GoCardlessEvent> events = Collections.singletonList(goCardlessEvent);
        webhookGoCardlessService.handleEvents(events);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
    }
}
