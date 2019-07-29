package uk.gov.pay.directdebit.mandate.services.sandbox;

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
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.mandate.services.GovUkPayEventToMandateStateMapper.GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_MANDATE_STATE;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GovUkPayEventFixture.aGovUkPayEventFixture;

@RunWith(JUnitParamsRunner.class)
public class SandboxMandateStateCalculatorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private GovUkPayEventDao mockGovUkPayEventDao;

    @InjectMocks
    private SandboxMandateStateCalculator sandboxMandateStateCalculator;

    private Mandate mandate;

    @Before
    public void setUp() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withPaymentProvider(SANDBOX);

        mandate = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .toEntity();
    }

    @Test
    @Parameters({
            "MANDATE_CREATED, CREATED",
            "MANDATE_TOKEN_EXCHANGED, AWAITING_DIRECT_DEBIT_DETAILS",
            "MANDATE_SUBMITTED_TO_PROVIDER, SUBMITTED_TO_PROVIDER",
            "MANDATE_USER_SETUP_EXPIRED, USER_SETUP_EXPIRED",
            "MANDATE_USER_SETUP_CANCELLED, USER_SETUP_CANCELLED",
            "MANDATE_USER_SETUP_CANCELLED_NOT_ELIGIBLE, USER_SETUP_CANCELLED"
    })
    public void govUkPayEventTypeMapsToState(String eventType, String expectedState) {
        GovUkPayEventType govUkPayEventType = GovUkPayEventType.valueOf(eventType);
        GovUkPayEvent govUkPayEvent = aGovUkPayEventFixture().withEventType(govUkPayEventType).toEntity();
        given(mockGovUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_MANDATE_STATE))
                .willReturn(Optional.of(govUkPayEvent));

        Optional<DirectDebitStateWithDetails<MandateState>> result = sandboxMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.valueOf(expectedState)));
    }

    @Test
    public void noApplicableEventsMapsToNothing() {
        Optional<DirectDebitStateWithDetails<MandateState>> result = sandboxMandateStateCalculator.calculate(mandate);

        assertThat(result, is(Optional.empty()));
    }
}
