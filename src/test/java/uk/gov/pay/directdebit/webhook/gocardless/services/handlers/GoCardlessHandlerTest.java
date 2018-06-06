package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.fixtures.EventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.model.Event;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessHandlerTest {

    GoCardlessHandler goCardlessHandler;

    @Mock
    TransactionService mockedTransactionService;

    @Mock
    PayerService mockedPayerService;

    @Mock
    GoCardlessService mockedGoCardlessService;

    @Spy
    GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().toEntity();

    Event event = EventFixture.aPaymentRequestEventFixture().toEntity();

    @Test
    public void shouldLinkToPaymentRequestEventAndStoreEventIfActionIsHandled() {
        goCardlessHandler = new GoCardlessHandler(mockedTransactionService, mockedGoCardlessService) {
            protected Map<GoCardlessAction, Function<Transaction, Event>> getHandledActions() {
                return ImmutableMap.of(
                        GoCardlessPaymentHandler.GoCardlessPaymentAction.PAID_OUT,
                        transaction -> event);
            }

            @Override
            protected Optional<Event> process(GoCardlessEvent event) {
                return Optional.of(GoCardlessHandlerTest.this.event);
            }
        };
        goCardlessHandler.handle(goCardlessEvent);
        verify(goCardlessEvent).setEventId(event.getId());
        verify(mockedGoCardlessService).updateInternalEventId(goCardlessEvent);
    }
}
