package uk.gov.pay.directdebit.events.model;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_SUBMITTED;

public class GovUkPayEventStateGraphForPaymentsTest {

    private GovUkPayEventStateGraph govUkPayEventStateGraph;

    @Before
    public void setUp() {
        govUkPayEventStateGraph = new GovUkPayEventStateGraph();
    }

    @Test
    public void shouldReturnTrueForPaymentInitialState() {
        boolean result = govUkPayEventStateGraph.isValidStartValue(PAYMENT_CREATED);
        assertThat(result, is(true));
    }
    
    @Test
    public void shouldReturnTrueForValidTransition() {
        boolean result = govUkPayEventStateGraph.isValidTransition(PAYMENT_CREATED, PAYMENT_SUBMITTED);
        assertThat(result, is(true));
    }
    
    @Test
    public void shouldReturnFalseForInvalidTransition() {
        boolean result = govUkPayEventStateGraph.isValidTransition(PAYMENT_SUBMITTED, PAYMENT_SUBMITTED);
        assertThat(result, is(false));
    }
}
