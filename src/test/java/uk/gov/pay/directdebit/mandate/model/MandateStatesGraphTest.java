package uk.gov.pay.directdebit.mandate.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.mandate.model.MandateState.FAILED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.PENDING;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_FAILED;

public class MandateStatesGraphTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private MandateStatesGraph mandateStatesGraph;

    @Before
    public void setup() {
        mandateStatesGraph = MandateStatesGraph.getStates();
    }

    @Test
    public void initialState_shouldReturnInitialState() {
        assertThat(MandateStatesGraph.initialState(), is(PENDING));
    }

    @Test
    public void getNextStateForEvent_shouldGiveTheNextStateIfEventIsValid() {
        assertThat(mandateStatesGraph.getNextStateForEvent(PENDING, MANDATE_ACTIVE), is(MandateState.ACTIVE));
    }

    @Test
    public void getNextStateForEvent_shouldThrowExceptionIfTransitionIsInvalid() {
        thrown.expect(InvalidStateTransitionException.class);
        thrown.expectMessage("Transition MANDATE_ACTIVE from state FAILED is not valid");
        thrown.reportMissingExceptionWithMessage("InvalidStateTransitionException expected");
        mandateStatesGraph.getNextStateForEvent(FAILED, MANDATE_ACTIVE);
    }

    @Test
    public void isValidTransition_shouldReturnTrueFromWhenTransitionIsExpected() {
        assertThat(mandateStatesGraph.isValidTransition(MandateState.PENDING, MandateState.ACTIVE, MANDATE_ACTIVE), is(true));
    }

    @Test
    public void isValidTransition_shouldReturnFalseWhenTransitionIsInvalid() {
        assertThat(mandateStatesGraph.isValidTransition(MandateState.CANCELLED, MandateState.PENDING, MANDATE_FAILED), is(false));
    }

    @Test
    public void priorStates() {
        assertThat(mandateStatesGraph.getPriorStates(MandateState.PENDING), is(new HashSet(Arrays.asList(MandateState.SUBMITTED, MandateState.CREATED, MandateState.AWAITING_DIRECT_DEBIT_DETAILS))));
    }
    
    
}

