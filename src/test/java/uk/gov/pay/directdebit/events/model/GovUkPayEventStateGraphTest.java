package uk.gov.pay.directdebit.events.model;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_USER_SETUP_CANCELLED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_USER_SETUP_EXPIRED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_SUBMITTED;

public class GovUkPayEventStateGraphTest {

    private GovUkPayEventStateGraph govUkPayEventStateGraph;

    @Before
    public void setUp() {
        govUkPayEventStateGraph = new GovUkPayEventStateGraph();
    }

    @Test
    public void isValidTransition_shouldReturnTrueForValidTransition() {
        boolean validTransition = govUkPayEventStateGraph.isValidTransition(MANDATE_CREATED, MANDATE_USER_SETUP_EXPIRED);
        assertThat(validTransition, is(true));
    }

    @Test
    public void isValidTransition_shouldReturnFalseForInvalidTransition() {
        boolean validTransition = govUkPayEventStateGraph.isValidTransition(MANDATE_USER_SETUP_EXPIRED, MANDATE_CREATED);
        assertThat(validTransition, is(false));
    }

    @Test
    public void isValidTransition_shouldReturnFalseForSameStatus() {
        boolean validTransition = govUkPayEventStateGraph.isValidTransition(MANDATE_CREATED, MANDATE_CREATED);
        assertThat(validTransition, is(false));
    }
    
    @Test 
    public void isValidStartState_shouldReturnTrueForMandateInitialState() {
        boolean validStartValue = govUkPayEventStateGraph.isValidStartValue(MANDATE_CREATED);
        assertThat(validStartValue, is(true));
    }

    @Test 
    public void isValidStartState_shouldReturnTrueForPaymentInitialState() {
        boolean validStartValue = govUkPayEventStateGraph.isValidStartValue(PAYMENT_SUBMITTED);
        assertThat(validStartValue, is(true));
    }

    @Test
    public void isValidStartState_shouldReturnFalseForNotInitialState() {
        boolean validStartValue = govUkPayEventStateGraph.isValidStartValue(MANDATE_USER_SETUP_CANCELLED);
        assertThat(validStartValue, is(false));
    }
}
