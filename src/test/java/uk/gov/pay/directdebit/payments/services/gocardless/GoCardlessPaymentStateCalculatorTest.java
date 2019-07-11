package uk.gov.pay.directdebit.payments.services.gocardless;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdAndOrganisationId;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.pay.directdebit.payments.services.gocardless.GoCardlessPaymentStateCalculator.GOCARDLESS_ACTIONS_THAT_CHANGE_STATE;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessPaymentStateCalculatorTest {

    @Mock
    private GoCardlessPaymentIdAndOrganisationId mockGoCardlessPaymentIdAndOrganisationId;

    @Mock
    private GoCardlessEvent mockGoCardlessEvent;

    @Mock
    private GoCardlessEventDao mockGoCardlessEventDao;

    private GoCardlessPaymentStateCalculator goCardlessPaymentStateCalculator;

    @Before
    public void setUp() {
        given(mockGoCardlessEventDao.findLatestApplicableEventForPayment(mockGoCardlessPaymentIdAndOrganisationId, GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.of(mockGoCardlessEvent));

        goCardlessPaymentStateCalculator = new GoCardlessPaymentStateCalculator(mockGoCardlessEventDao);
    }

    @Test
    public void failedActionMapsToFailedState() {
        given(mockGoCardlessEvent.getAction()).willReturn("failed");

        Optional<PaymentState> paymentState = goCardlessPaymentStateCalculator.calculate(mockGoCardlessPaymentIdAndOrganisationId);

        assertThat(paymentState.get(), is(PaymentState.FAILED));
    }

    @Test
    public void paidOutActionMapsToSuccessState() {
        given(mockGoCardlessEvent.getAction()).willReturn("paid_out");

        Optional<PaymentState> paymentState = goCardlessPaymentStateCalculator.calculate(mockGoCardlessPaymentIdAndOrganisationId);

        assertThat(paymentState.get(), is(PaymentState.SUCCESS));
    }

    @Test
    public void unrecognisedActionMapsToNothing() {
        given(mockGoCardlessEvent.getAction()).willReturn("eaten_by_wolves");

        Optional<PaymentState> paymentState = goCardlessPaymentStateCalculator.calculate(mockGoCardlessPaymentIdAndOrganisationId);

        assertThat(paymentState, is(Optional.empty()));
    }

    @Test
    public void noApplicableEventsMapsToNothing() {
        given(mockGoCardlessEventDao.findLatestApplicableEventForPayment(mockGoCardlessPaymentIdAndOrganisationId, GOCARDLESS_ACTIONS_THAT_CHANGE_STATE))
                .willReturn(Optional.empty());

        Optional<PaymentState> paymentState = goCardlessPaymentStateCalculator.calculate(mockGoCardlessPaymentIdAndOrganisationId);

        assertThat(paymentState, is(Optional.empty()));
    }

}
