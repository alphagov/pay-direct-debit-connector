package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.GoCardlessEventService;
import uk.gov.pay.directdebit.payments.services.PaymentService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessHandlerTest {

    GoCardlessHandler goCardlessHandler;

    @Mock
    PaymentService mockedPaymentService;

    @Mock
    GoCardlessEventService mockedGoCardlessService;

    @Spy
    GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().toEntity();

    DirectDebitEvent directDebitEvent = DirectDebitEventFixture.aDirectDebitEventFixture().toEntity();

    @Test
    public void shouldLinkToDirectDebitEventAndStoreEventIfActionIsHandled() {
        goCardlessHandler = new GoCardlessHandler(mockedPaymentService, mockedGoCardlessService) {
            protected Map<GoCardlessAction, Function<Payment, DirectDebitEvent>> getHandledActions() {
                return ImmutableMap.of(
                        GoCardlessPaymentHandler.GoCardlessPaymentAction.PAID_OUT,
                        transaction -> directDebitEvent);
            }

            @Override
            protected Optional<DirectDebitEvent> process(GoCardlessEvent event) {
                return Optional.of(GoCardlessHandlerTest.this.directDebitEvent);
            }
        };
        goCardlessHandler.handle(goCardlessEvent);
        verify(goCardlessEvent).setInternalEventId(directDebitEvent.getId());
        verify(mockedGoCardlessService).updateInternalEventId(goCardlessEvent);
    }
}
