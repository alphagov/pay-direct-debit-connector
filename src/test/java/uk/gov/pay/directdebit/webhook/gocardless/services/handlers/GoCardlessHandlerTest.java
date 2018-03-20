package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessHandlerTest {

    GoCardlessHandler goCardlessHandler;

    @Mock
    TransactionService mockedTransactionService;

    @Mock
    GoCardlessService mockedGoCardlessService;

    @Spy
    GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().toEntity();

    PaymentRequestEvent paymentRequestEvent = PaymentRequestEventFixture.aPaymentRequestEventFixture().toEntity();

    @Test
    public void shouldLinkToPaymentRequestEventAndStoreEventIfActionIsHandled() {
        goCardlessHandler = new GoCardlessHandler(mockedTransactionService, mockedGoCardlessService) {
            @Override
            protected Map<GoCardlessAction, Function<Transaction, PaymentRequestEvent>> getHandledActions() {
                return ImmutableMap.of(
                        GoCardlessPaymentHandler.GoCardlessPaymentAction.PAID_OUT,
                        transaction -> paymentRequestEvent);
            }

            @Override
            protected Optional<PaymentRequestEvent> process(GoCardlessEvent event) {
                return Optional.of(paymentRequestEvent);
            }
        };
        goCardlessHandler.handle(goCardlessEvent);
        verify(goCardlessEvent).setPaymentRequestEventId(paymentRequestEvent.getId());
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
    }

    @Test
    public void shouldNotLinkToPaymentRequestEventButStillStoreEventIfActionIsNotHandled() {
        goCardlessHandler = new GoCardlessHandler(mockedTransactionService, mockedGoCardlessService) {
            @Override
            protected Map<GoCardlessAction, Function<Transaction, PaymentRequestEvent>> getHandledActions() {
                return ImmutableMap.of();
            }

            @Override
            protected Optional<PaymentRequestEvent> process(GoCardlessEvent event) {
                return Optional.empty();
            }
        };
        goCardlessHandler.handle(goCardlessEvent);
        verify(goCardlessEvent, never()).setPaymentRequestEventId(anyLong());
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
    }
}
