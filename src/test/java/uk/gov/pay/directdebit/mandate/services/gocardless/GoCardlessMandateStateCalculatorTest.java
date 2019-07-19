package uk.gov.pay.directdebit.mandate.services.gocardless;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessMandateStateCalculator.GOCARDLESS_ACTIONS_THAT_CHANGE_STATE;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessMandateStateCalculatorTest {

    @Mock
    private GoCardlessEvent mockGoCardlessEvent;

    @Mock
    private GoCardlessEventDao mockGoCardlessEventDao;

    private GoCardlessMandateStateCalculator goCardlessMandateStateCalculator;

    private GoCardlessMandateId goCardlessMandateId = GoCardlessMandateId.valueOf("a-mandate-id");

    private GoCardlessOrganisationId goCardlessOrganisationId = GoCardlessOrganisationId.valueOf("an-organisation-id");

    private Mandate mandate;
    
    @Before
    public void setUp() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withPaymentProvider(GOCARDLESS)
                .withOrganisation(goCardlessOrganisationId);

        mandate = aMandateFixture()
                .withPaymentProviderId(goCardlessMandateId)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .toEntity();
        
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateId, goCardlessOrganisationId, GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(mockGoCardlessEvent));

        goCardlessMandateStateCalculator = new GoCardlessMandateStateCalculator(mockGoCardlessEventDao);
    }

    @Test
    public void createdActionMapsToCreatedState() {
        given(mockGoCardlessEvent.getAction()).willReturn("created");

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.CREATED));
    }

    @Test
    public void submittedActionMapsToSubmittedState() {
        given(mockGoCardlessEvent.getAction()).willReturn("submitted");

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.SUBMITTED));
    }

    @Test
    public void activeActionMapsToActiveState() {
        given(mockGoCardlessEvent.getAction()).willReturn("active");

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.ACTIVE));
    }

    @Test
    public void cancelledActionMapsToCancelledState() {
        given(mockGoCardlessEvent.getAction()).willReturn("cancelled");

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.CANCELLED));
    }

    @Test
    public void failedActionMapsToFailedState() {
        given(mockGoCardlessEvent.getAction()).willReturn("failed");

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getState(), is(MandateState.FAILED));
    }

    @Test
    public void detailsCauseAndDescriptionReturned() {
        given(mockGoCardlessEvent.getAction()).willReturn("failed");
        given(mockGoCardlessEvent.getDetailsCause()).willReturn("details_cause");
        given(mockGoCardlessEvent.getDetailsDescription()).willReturn("This is a description.");

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result.get().getDetails(), is(Optional.of("details_cause")));
        assertThat(result.get().getDetailsDescription(), is(Optional.of("This is a description.")));
    }

    @Test
    public void unrecognisedActionMapsToNothing() {
        given(mockGoCardlessEvent.getAction()).willReturn("eaten_by_wolves");

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void noApplicableEventsMapsToNothing() {
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(goCardlessMandateId, goCardlessOrganisationId, GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.empty());

        Optional<DirectDebitStateWithDetails<MandateState>> result = goCardlessMandateStateCalculator.calculate(mandate);

        assertThat(result, is(Optional.empty()));
    }

}
