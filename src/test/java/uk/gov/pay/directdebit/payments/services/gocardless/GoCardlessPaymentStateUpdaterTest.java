package uk.gov.pay.directdebit.payments.services.gocardless;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdAndOrganisationId;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.services.PaymentUpdateService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessPaymentStateUpdaterTest {

    private static final GoCardlessPaymentIdAndOrganisationId GOCARDLESS_PAYMENT_ID_AND_ORGANISATION_ID = new GoCardlessPaymentIdAndOrganisationId(
            GoCardlessPaymentId.valueOf("PM123"), GoCardlessOrganisationId.valueOf("OR123"));

    @Mock
    private DirectDebitStateWithDetails<PaymentState> mockPaymentStateWithDetails;

    @Mock
    private PaymentUpdateService mockPaymentUpdateService;

    @Mock
    private GoCardlessPaymentStateCalculator mockGoCardlessPaymentStateCalculator;

    private GoCardlessPaymentStateUpdater mockGoCardlessPaymentStateUpdater;

    @Before
    public void setUp() {
        mockGoCardlessPaymentStateUpdater = new GoCardlessPaymentStateUpdater(mockPaymentUpdateService, mockGoCardlessPaymentStateCalculator);
    }

    @Test
    public void updatesPaymentWithStateReturnedByCalculator() {
        given(mockGoCardlessPaymentStateCalculator.calculate(GOCARDLESS_PAYMENT_ID_AND_ORGANISATION_ID)).willReturn(Optional.of(mockPaymentStateWithDetails));

        mockGoCardlessPaymentStateUpdater.updateState(GOCARDLESS_PAYMENT_ID_AND_ORGANISATION_ID);

        verify(mockPaymentUpdateService).updateStateByProviderId(GOCARDLESS, GOCARDLESS_PAYMENT_ID_AND_ORGANISATION_ID, mockPaymentStateWithDetails);
    }

    @Test
    public void updatesNothingIfCalculatorDoesNotReturnState() {
        given(mockGoCardlessPaymentStateCalculator.calculate(GOCARDLESS_PAYMENT_ID_AND_ORGANISATION_ID)).willReturn(Optional.empty());

        mockGoCardlessPaymentStateUpdater.updateState(GOCARDLESS_PAYMENT_ID_AND_ORGANISATION_ID);

        verify(mockPaymentUpdateService, never()).updateStateByProviderId(any(), any(), any());
    }

}
