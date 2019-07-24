package uk.gov.pay.directdebit.payments.services.gocardless;

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
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;
import static uk.gov.pay.directdebit.payments.services.gocardless.GoCardlessPaymentStateCalculator.GOCARDLESS_ACTIONS_THAT_CHANGE_STATE;

@RunWith(JUnitParamsRunner.class)
public class GoCardlessPaymentStateCalculatorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private GoCardlessEventDao mockGoCardlessEventDao;

    @InjectMocks
    private GoCardlessPaymentStateCalculator goCardlessPaymentStateCalculator;

    private GoCardlessOrganisationId goCardlessOrganisationId = GoCardlessOrganisationId.valueOf("an-organisation-id");

    private GoCardlessPaymentId goCardlessPaymentId = GoCardlessPaymentId.valueOf("a-payment-id");

    private Payment payment;

    @Before
    public void setUp() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withPaymentProvider(PaymentProvider.GOCARDLESS)
                .withOrganisation(goCardlessOrganisationId);

        MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);

        payment = aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withPaymentProviderId(goCardlessPaymentId)
                .toEntity();
    }

    @Test
    @Parameters({
            "failed, FAILED",
            "paid_out, SUCCESS"
    })
    public void goCardlessEventActionMapsToState(String action, String expectedState) {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withAction(action).toEntity();
        given(mockGoCardlessEventDao.findLatestApplicableEventForPayment(goCardlessPaymentId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = goCardlessPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getState(), is(PaymentState.valueOf(expectedState)));
    }

    @Test
    public void detailsCauseAndDescriptionReturned() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withAction("failed")
                .withDetailsCause("details_cause")
                .withDetailsDescription("This is a description")
                .toEntity();

        given(mockGoCardlessEventDao.findLatestApplicableEventForPayment(goCardlessPaymentId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = goCardlessPaymentStateCalculator.calculate(payment);

        assertThat(result.get().getDetails(), is(Optional.of(goCardlessEvent.getDetailsCause())));
        assertThat(result.get().getDetailsDescription(), is(Optional.of(goCardlessEvent.getDetailsDescription())));
    }

    @Test
    public void unrecognisedActionMapsToNothing() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().withAction("eaten_by_wolves").toEntity();
        
        given(mockGoCardlessEventDao.findLatestApplicableEventForPayment(goCardlessPaymentId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(goCardlessEvent));

        Optional<DirectDebitStateWithDetails<PaymentState>> result = goCardlessPaymentStateCalculator.calculate(payment);

        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void noApplicableEventsMapsToNothing() {
        given(mockGoCardlessEventDao.findLatestApplicableEventForPayment(goCardlessPaymentId, goCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.empty());

        Optional<DirectDebitStateWithDetails<PaymentState>> result = goCardlessPaymentStateCalculator.calculate(payment);

        assertThat(result, is(Optional.empty()));
    }

}
