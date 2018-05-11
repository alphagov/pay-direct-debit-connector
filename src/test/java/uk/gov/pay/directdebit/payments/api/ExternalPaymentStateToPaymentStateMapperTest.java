package uk.gov.pay.directdebit.payments.api;

import org.junit.Test;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExternalPaymentStateToPaymentStateMapperTest {

    @Test
    public void shouldReturnAListOf3PaymentStates_whenExternalStarted() {
        List<PaymentState> paymentStates = ExternalPaymentStateToPaymentStateMapper.getPaymentState(ExternalPaymentState.EXTERNAL_STARTED);
        assertThat(paymentStates.size(), is(3));
        assertThat(paymentStates.contains(PaymentState.NEW), is(true));
        assertThat(paymentStates.contains(PaymentState.PENDING_DIRECT_DEBIT_PAYMENT), is(false));
    }
}
