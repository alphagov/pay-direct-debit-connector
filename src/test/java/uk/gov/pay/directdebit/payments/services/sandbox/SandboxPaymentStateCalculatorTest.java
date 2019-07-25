package uk.gov.pay.directdebit.payments.services.sandbox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.SandboxEventDao;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.webhook.sandbox.resources.WebhookSandboxResource.SandboxEventAction;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;
import static uk.gov.pay.directdebit.payments.services.sandbox.SandboxPaymentStateCalculator.SANDBOX_ACTIONS_THAT_CHANGE_STATE;

@RunWith(MockitoJUnitRunner.class)
public class SandboxPaymentStateCalculatorTest {

    private static final SandboxPaymentId SANDBOX_PAYMENT_ID = SandboxPaymentId.valueOf("Sandbox payment ID");

    @Mock
    private SandboxEvent mockSandboxEvent;

    @Mock
    private SandboxEventDao mockSandboxEventDao;

    @InjectMocks
    private SandboxPaymentStateCalculator sandboxPaymentStateCalculator;
    
    private Payment payment;
    
    @Before
    public void setUp() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withPaymentProvider(SANDBOX);

        MandateFixture mandateFixture = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture);
        
        payment = aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withPaymentProviderId(SANDBOX_PAYMENT_ID)
                .toEntity();

        given(mockSandboxEventDao.findLatestApplicableEventForPayment(SANDBOX_PAYMENT_ID, SANDBOX_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(mockSandboxEvent));
    }

    @Test
    public void paidOutActionMapsToSuccessState() {
        given(mockSandboxEvent.getEventAction()).willReturn(SandboxEventAction.PAID_OUT.toString());
        
        Optional<DirectDebitStateWithDetails<PaymentState>> result = sandboxPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getState(), is(PaymentState.SUCCESS));
    }

    @Test
    public void unrecognisedActionMapsToNothing() {
        given(mockSandboxEvent.getEventAction()).willReturn("EATEN_BY_WOLVES");

        Optional<DirectDebitStateWithDetails<PaymentState>> result = sandboxPaymentStateCalculator.calculate(payment);

        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void noApplicableEventsMapsToNothing() {
        given(mockSandboxEventDao.findLatestApplicableEventForPayment(SANDBOX_PAYMENT_ID, SANDBOX_ACTIONS_THAT_CHANGE_STATE)).willReturn(Optional.empty());

        Optional<DirectDebitStateWithDetails<PaymentState>> result = sandboxPaymentStateCalculator.calculate(payment);

        assertThat(result, is(Optional.empty()));
    }

}
