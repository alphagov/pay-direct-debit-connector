package uk.gov.pay.directdebit.payments.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED;
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
        assertThat(paymentStatesGraph.getNextStateForEvent(NEW, TOKEN_EXCHANGED), is(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS));
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
        assertThat(paymentStatesGraph.isValidTransition(PaymentState.NEW, PaymentState.AWAITING_DIRECT_DEBIT_DETAILS, TOKEN_EXCHANGED), is(true));
    }

    @Test
    public void isValidTransition_shouldReturnFalseWhenTransitionIsInvalid() {
        assertThat(paymentStatesGraph.isValidTransition(PaymentState.NEW, PaymentState.AWAITING_DIRECT_DEBIT_DETAILS, CHARGE_CREATED), is(false));
    }
}

