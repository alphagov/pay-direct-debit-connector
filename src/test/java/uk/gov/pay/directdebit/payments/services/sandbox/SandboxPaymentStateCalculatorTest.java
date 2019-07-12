package uk.gov.pay.directdebit.payments.services.sandbox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.dao.SandboxEventDao;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.webhook.sandbox.resources.WebhookSandboxResource.SandboxEventAction;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.payments.services.sandbox.SandboxPaymentStateCalculator.SANDBOX_ACTIONS_THAT_CHANGE_STATE;

@RunWith(MockitoJUnitRunner.class)
public class SandboxPaymentStateCalculatorTest {

    private static final SandboxPaymentId SANDBOX_PAYMENT_ID = SandboxPaymentId.valueOf("Sandbox payment ID");

    @Mock
    private SandboxEvent mockSandboxEvent;

    @Mock
    private SandboxEventDao mockSandboxEventDao;

    private SandboxPaymentStateCalculator sandboxPaymentStateCalculator;

    @Before
    public void setUp() {
        given(mockSandboxEventDao.findLatestApplicableEventForPayment(SANDBOX_PAYMENT_ID, SANDBOX_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(mockSandboxEvent));

        sandboxPaymentStateCalculator = new SandboxPaymentStateCalculator(mockSandboxEventDao);
    }

    @Test
    public void paidOutActionMapsToSuccessState() {
        given(mockSandboxEvent.getEventAction()).willReturn(SandboxEventAction.PAID_OUT.toString());

        Optional<PaymentState> paymentState = sandboxPaymentStateCalculator.calculate(SANDBOX_PAYMENT_ID);

        assertThat(paymentState.get(), is(PaymentState.SUCCESS));
    }

    @Test
    public void unrecognisedActionMapsToNothing() {
        given(mockSandboxEvent.getEventAction()).willReturn("EATEN_BY_WOLVES");

        Optional<PaymentState> paymentState = sandboxPaymentStateCalculator.calculate(SANDBOX_PAYMENT_ID);

        assertThat(paymentState, is(Optional.empty()));
    }

    @Test
    public void noApplicableEventsMapsToNothing() {
        given(mockSandboxEventDao.findLatestApplicableEventForPayment(SANDBOX_PAYMENT_ID, SANDBOX_ACTIONS_THAT_CHANGE_STATE)).willReturn(Optional.empty());

        Optional<PaymentState> paymentState = sandboxPaymentStateCalculator.calculate(SANDBOX_PAYMENT_ID);

        assertThat(paymentState, is(Optional.empty()));
    }

}
