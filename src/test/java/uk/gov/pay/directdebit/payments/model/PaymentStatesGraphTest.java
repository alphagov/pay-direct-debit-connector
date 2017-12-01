package uk.gov.pay.directdebit.payments.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.*;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.SYSTEM_CANCEL;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.WEBHOOK_ACTION_PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.PaymentState.*;

public class PaymentStatesGraphTest {
    private PaymentStatesGraph paymentStatesGraph;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        assertThat(paymentStatesGraph.getNextStateForEvent(NEW, SYSTEM_CANCEL), is(PaymentState.SYSTEM_CANCELLED));
    }

    @Test
    public void getNextStateForEvent_shouldThrowExceptionIfTransitionIsInvalid() {
        thrown.expect(InvalidStateTransitionException.class);
        thrown.expectMessage("Transition WEBHOOK_ACTION_PAID_OUT from state NEW is not valid");
        thrown.reportMissingExceptionWithMessage("InvalidStateTransitionException expected");
        paymentStatesGraph.getNextStateForEvent(NEW, WEBHOOK_ACTION_PAID_OUT);
    }

    @Test
    public void isValidTransition_shouldReturnTrueFromWhenTransitionIsExpected() {
        assertThat(paymentStatesGraph.isValidTransition(PaymentState.NEW, PaymentState.ENTER_DIRECT_DEBIT_DETAILS, SupportedEvent.SHOW_ENTER_DIRECT_DEBIT_DETAILS), is(true));
    }
    @Test
    public void isValidTransition_shouldReturnFalseWhenTransitionIsInvalid() {
        assertThat(paymentStatesGraph.isValidTransition(PaymentState.NEW, PaymentState.ENTER_DIRECT_DEBIT_DETAILS, SupportedEvent.SYSTEM_CANCEL), is(false));
    }
}

