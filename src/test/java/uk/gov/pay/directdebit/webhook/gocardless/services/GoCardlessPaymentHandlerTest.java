package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessPaymentHandlerTest {

    @Mock
    TransactionService mockedTransactionService;
    @Mock
    GoCardlessService mockedGoCardlessService;

    private GoCardlessPaymentFixture goCardlessPaymentFixture = GoCardlessPaymentFixture.aGoCardlessPaymentFixture();
    private GoCardlessPaymentHandler goCardlessPaymentHandler;
    @Before
    public void setUp() {
        goCardlessPaymentHandler = new GoCardlessPaymentHandler(mockedTransactionService, mockedGoCardlessService);
    }

    @Test
    public void shouldHandleEventsWithAValidAction() {
        GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().withAction("paid_out").toEntity();
        PaymentRequestEvent paymentRequestEvent = PaymentRequestEventFixture.aPaymentRequestEventFixture().toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();
        when(mockedGoCardlessService.findPaymentForEvent(goCardlessEvent)).thenReturn(goCardlessPaymentFixture.toEntity());
        when(mockedTransactionService.findTransactionFor(goCardlessPaymentFixture.getTransactionId())).thenReturn(transaction);
        when(mockedTransactionService.paidOutFor(transaction)).thenReturn(paymentRequestEvent);
        goCardlessPaymentHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).paidOutFor(transaction);
        ArgumentCaptor<GoCardlessEvent> geCaptor = forClass(GoCardlessEvent.class);

        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void shouldStoreEventsWithAnInvalidAction() {
        GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().withAction("somethingelse").toEntity();
        goCardlessPaymentHandler.handle(goCardlessEvent);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
    }
}
