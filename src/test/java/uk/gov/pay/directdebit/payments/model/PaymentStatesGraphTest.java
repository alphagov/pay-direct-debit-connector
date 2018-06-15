package uk.gov.pay.directdebit.payments.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.PaymentState.NEW;

public class PaymentStatesGraphTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private PaymentStatesGraph paymentStatesGraph;

    @Before
    public void setup() {
        paymentStatesGraph = PaymentStatesGraph.getStates();
    }

    @Test
    public void initialState_shouldReturnInitialState() {
        assertThat(PaymentStatesGraph.initialState(), is(NEW));
    }

    @Test
    public void getNextStateForEvent_shouldGiveTheNextStateIfEventIsValid() {
        assertThat(paymentStatesGraph.getNextStateForEvent(NEW, PAYMENT_SUBMITTED_TO_PROVIDER), is(PaymentState.PENDING));
    }

    @Test
    public void getNextStateForEvent_shouldThrowExceptionIfTransitionIsInvalid() {
        thrown.expect(InvalidStateTransitionException.class);
        thrown.expectMessage("Transition CHARGE_CREATED from state NEW is not valid");
        thrown.reportMissingExceptionWithMessage("InvalidStateTransitionException expected");
        paymentStatesGraph.getNextStateForEvent(NEW, CHARGE_CREATED);
    }

    @Test
    public void isValidTransition_shouldReturnTrueFromWhenTransitionIsExpected() {
        assertThat(paymentStatesGraph.isValidTransition(PaymentState.NEW, PaymentState.PENDING, PAYMENT_SUBMITTED_TO_PROVIDER), is(true));
    }

    @Test
    public void isValidTransition_shouldReturnFalseWhenTransitionIsInvalid() {
        assertThat(paymentStatesGraph.isValidTransition(PaymentState.NEW, PaymentState.NEW, CHARGE_CREATED), is(false));
    }
}

