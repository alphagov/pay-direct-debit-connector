package uk.gov.pay.directdebit.payments.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CREATED;

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
        assertThat(PaymentStatesGraph.initialState(), is(CREATED));
    }

    @Test
    public void getNextStateForEvent_shouldGiveTheNextStateIfEventIsValid() {
        assertThat(paymentStatesGraph.getNextStateForEvent(CREATED, PAYMENT_SUBMITTED_TO_PROVIDER), is(Optional.of(PaymentState.SUBMITTED_TO_PROVIDER)));
    }

    @Test
    public void getNextStateForEvent_shouldReturnEmptyIfTransitionIsInvalid() {
        assertThat(paymentStatesGraph.getNextStateForEvent(CREATED, CHARGE_CREATED), is(Optional.empty()));
    }

    @Test
    public void isValidTransition_shouldReturnTrueFromWhenTransitionIsExpected() {
        assertThat(paymentStatesGraph.isValidTransition(PaymentState.CREATED, PaymentState.SUBMITTED_TO_PROVIDER, PAYMENT_SUBMITTED_TO_PROVIDER), is(true));
    }

    @Test
    public void isValidTransition_shouldReturnFalseWhenTransitionIsInvalid() {
        assertThat(paymentStatesGraph.isValidTransition(PaymentState.CREATED, PaymentState.CREATED, CHARGE_CREATED), is(false));
    }
}

