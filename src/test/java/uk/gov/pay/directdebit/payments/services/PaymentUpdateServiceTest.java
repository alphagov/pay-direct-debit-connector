package uk.gov.pay.directdebit.payments.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PENDING;

@RunWith(MockitoJUnitRunner.class)
public class PaymentUpdateServiceTest {
    
    @Mock
    private PaymentDao mockPaymentDao;

    @InjectMocks
    private PaymentUpdateService paymentUpdateService;

    private Payment payment = aPaymentFixture()
            .withStateDetails("details")
            .withStateDetailsDescription("description")
            .toEntity();

    @Test
    public void callsToUpdateStateAndReturnsUpdatedPayment_whenDetailsAndDescriptionAreEmpty() {
        PaymentState state = PENDING;
        DirectDebitStateWithDetails<PaymentState> stateWithDetails = new DirectDebitStateWithDetails<>(state);

        Payment updatedPayment = paymentUpdateService.updateState(payment, stateWithDetails);

        verify(mockPaymentDao).updateStateAndDetails(payment.getId(), state, null, null);

        assertThat(updatedPayment.getExternalId(), is(payment.getExternalId()));
        assertThat(updatedPayment.getState(), is(state));
        assertThat(updatedPayment.getStateDetails(), is(Optional.empty()));
        assertThat(updatedPayment.getStateDetailsDescription(), is(Optional.empty()));
    }

    @Test
    public void callsToUpdateStateAndReturnsUpdatedPayment_withDetailsAndDescription() {
        PaymentState state = PENDING;
        String details = "new-details";
        String description = "new-description";
        DirectDebitStateWithDetails<PaymentState> stateWithDetails = new DirectDebitStateWithDetails<>(state, details, description);

        Payment updatedPayment = paymentUpdateService.updateState(payment, stateWithDetails);

        verify(mockPaymentDao).updateStateAndDetails(payment.getId(), state, details, description);

        assertThat(updatedPayment.getExternalId(), is(payment.getExternalId()));
        assertThat(updatedPayment.getState(), is(state));
        assertThat(updatedPayment.getStateDetails(), is(Optional.of(details)));
        assertThat(updatedPayment.getStateDetailsDescription(), is(Optional.of(description)));
    }

}
