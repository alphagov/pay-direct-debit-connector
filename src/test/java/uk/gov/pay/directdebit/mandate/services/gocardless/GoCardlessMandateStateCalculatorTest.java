package uk.gov.pay.directdebit.mandate.services.gocardless;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessMandateStateCalculator.GOCARDLESS_ACTIONS_THAT_CHANGE_STATE;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessMandateStateCalculatorTest {

    @Mock
    private GoCardlessMandateId mockGoCardlessMandateId;

    @Mock
    private GoCardlessOrganisationId mockGoCardlessOrganisationId;
    
    @Mock
    private GoCardlessEvent mockGoCardlessEvent;

    @Mock
    private GoCardlessEventDao mockGoCardlessEventDao;

    private GoCardlessMandateStateCalculator goCardlessMandateStateCalculator;

    @Before
    public void setUp() {
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(mockGoCardlessMandateId, mockGoCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE)).willReturn(Optional.of(mockGoCardlessEvent));

        goCardlessMandateStateCalculator = new GoCardlessMandateStateCalculator(mockGoCardlessEventDao);
    }

    @Test
    public void createdActionMapsToCreatedState() {
        given(mockGoCardlessEvent.getAction()).willReturn("created");

        Optional<MandateState> mandateState = goCardlessMandateStateCalculator.calculate(mockGoCardlessMandateId, mockGoCardlessOrganisationId);
        
        assertThat(mandateState.get(), is(MandateState.CREATED));
    }

    @Test
    public void submittedActionMapsToSubmittedState() {
        given(mockGoCardlessEvent.getAction()).willReturn("submitted");

        Optional<MandateState> mandateState = goCardlessMandateStateCalculator.calculate(mockGoCardlessMandateId, mockGoCardlessOrganisationId);

        assertThat(mandateState.get(), is(MandateState.SUBMITTED));
    }

    @Test
    public void activeActionMapsToActiveState() {
        given(mockGoCardlessEvent.getAction()).willReturn("active");

        Optional<MandateState> mandateState = goCardlessMandateStateCalculator.calculate(mockGoCardlessMandateId, mockGoCardlessOrganisationId);

        assertThat(mandateState.get(), is(MandateState.ACTIVE));
    }

    @Test
    public void cancelledActionMapsToCancelledState() {
        given(mockGoCardlessEvent.getAction()).willReturn("cancelled");

        Optional<MandateState> mandateState = goCardlessMandateStateCalculator.calculate(mockGoCardlessMandateId, mockGoCardlessOrganisationId);

        assertThat(mandateState.get(), is(MandateState.CANCELLED));
    }

    @Test
    public void failedActionMapsToFailedState() {
        given(mockGoCardlessEvent.getAction()).willReturn("failed");

        Optional<MandateState> mandateState = goCardlessMandateStateCalculator.calculate(mockGoCardlessMandateId, mockGoCardlessOrganisationId);

        assertThat(mandateState.get(), is(MandateState.FAILED));
    }

    @Test
    public void unrecognisedActionMapsToNothing() {
        given(mockGoCardlessEvent.getAction()).willReturn("eaten_by_wolves");

        Optional<MandateState> mandateState = goCardlessMandateStateCalculator.calculate(mockGoCardlessMandateId, mockGoCardlessOrganisationId);

        assertThat(mandateState, is(Optional.empty()));
    }

    @Test
    public void noApplicableEventsMapsToNothing() {
        given(mockGoCardlessEventDao.findLatestApplicableEventForMandate(mockGoCardlessMandateId, mockGoCardlessOrganisationId,
                GOCARDLESS_ACTIONS_THAT_CHANGE_STATE)).willReturn(Optional.empty());

        Optional<MandateState> mandateState = goCardlessMandateStateCalculator.calculate(mockGoCardlessMandateId, mockGoCardlessOrganisationId);

        assertThat(mandateState, is(Optional.empty()));
    }

}
