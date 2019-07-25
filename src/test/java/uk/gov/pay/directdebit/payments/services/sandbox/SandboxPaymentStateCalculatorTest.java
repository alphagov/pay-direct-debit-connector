package uk.gov.pay.directdebit.payments.services.sandbox;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.dao.SandboxEventDao;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.events.model.SandboxEvent.SandboxEventBuilder.aSandboxEvent;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GovUkPayEventFixture.aGovUkPayEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;
import static uk.gov.pay.directdebit.payments.services.GovUkPayEventToPaymentStateMapper.GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_PAYMENT_STATE;
import static uk.gov.pay.directdebit.payments.services.sandbox.SandboxEventToPaymentStateMapper.SANDBOX_ACTIONS_THAT_CHANGE_PAYMENT_STATE;
import static uk.gov.pay.directdebit.webhook.sandbox.resources.WebhookSandboxResource.SandboxEventAction.PAID_OUT;

@RunWith(JUnitParamsRunner.class)
public class SandboxPaymentStateCalculatorTest {

    private static final SandboxPaymentId SANDBOX_PAYMENT_ID = SandboxPaymentId.valueOf("Sandbox payment ID");

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    @Mock
    private SandboxEventDao mockSandboxEventDao;

    @Mock
    private GovUkPayEventDao mockGovUkPayEventDao;

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
    }

    @Test
    @Parameters({
            "PAID_OUT, PAID_OUT"
    })
    public void sandboxEventActionMapsToState(String action, String expectedState) {
        SandboxEvent sandboxEvent = aSandboxEvent().withEventAction(action).build();

        given(mockSandboxEventDao.findLatestApplicableEventForPayment(SANDBOX_PAYMENT_ID, SANDBOX_ACTIONS_THAT_CHANGE_PAYMENT_STATE))
                .willReturn(Optional.of(sandboxEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = sandboxPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getState(), is(PaymentState.valueOf(expectedState)));
    }

    @Test
    @Parameters({
            "PAYMENT_SUBMITTED, SUBMITTED_TO_PROVIDER"
    })
    public void govUkPayEventTypeMapsToState(String eventType, String expectedState) {
        GovUkPayEventType govUkPayEventType = GovUkPayEventType.valueOf(eventType);
        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture().withEventType(govUkPayEventType).toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForPayment(payment.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_PAYMENT_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = sandboxPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getState(), is(PaymentState.valueOf(expectedState)));
    }

    @Test
    public void resolvesStateFromGovUkPayEventWhenIsLaterThanLatestSandboxEvent() {
        SandboxEvent sandboxEvent = aSandboxEvent()
                .withEventAction(PAID_OUT.toString())
                .withCreatedAt(ZonedDateTime.of(2019, 7, 22, 9, 0, 0, 0, UTC))
                .build();

        given(mockSandboxEventDao.findLatestApplicableEventForPayment(SANDBOX_PAYMENT_ID, SANDBOX_ACTIONS_THAT_CHANGE_PAYMENT_STATE))
                .willReturn(Optional.of(sandboxEvent));

        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture()
                .withEventType(PAYMENT_SUBMITTED)
                .withEventDate(ZonedDateTime.of(2019, 7, 22, 10, 0, 0, 0, UTC))
                .toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForPayment(payment.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_PAYMENT_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = sandboxPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getState(), is(PaymentState.SUBMITTED_TO_PROVIDER));
    }

    @Test
    public void resolvesStateFromSandboxEventWhenIsLaterThanLatestGovUkPayEvent() {
        SandboxEvent sandboxEvent = aSandboxEvent()
                .withEventAction(PAID_OUT.toString())
                .withCreatedAt(ZonedDateTime.of(2019, 7, 22, 10, 0, 0, 0, UTC))
                .build();

        given(mockSandboxEventDao.findLatestApplicableEventForPayment(SANDBOX_PAYMENT_ID, SANDBOX_ACTIONS_THAT_CHANGE_PAYMENT_STATE))
                .willReturn(Optional.of(sandboxEvent));

        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture()
                .withEventType(PAYMENT_SUBMITTED)
                .withEventDate(ZonedDateTime.of(2019, 7, 22, 9, 0, 0, 0, UTC))
                .toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForPayment(payment.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_PAYMENT_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = sandboxPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getState(), is(PaymentState.PAID_OUT));
    }

    @Test
    public void noApplicableEventsMapsToNothing() {
        given(mockSandboxEventDao.findLatestApplicableEventForPayment(SANDBOX_PAYMENT_ID, SANDBOX_ACTIONS_THAT_CHANGE_PAYMENT_STATE)).willReturn(Optional.empty());

        Optional<DirectDebitStateWithDetails<PaymentState>> result = sandboxPaymentStateCalculator.calculate(payment);

        assertThat(result, is(Optional.empty()));
    }

}
