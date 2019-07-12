package uk.gov.pay.directdebit.payments.services.sandbox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.payments.services.PaymentUpdateService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUCCESS;

@RunWith(MockitoJUnitRunner.class)
public class SandboxPaymentStateUpdaterTest {

    private static final SandboxPaymentId SANDBOX_PAYMENT_ID = SandboxPaymentId.valueOf("Sandbox payment ID");

    @Mock
    private PaymentUpdateService mockPaymentUpdateService;

    @Mock
    private SandboxPaymentStateCalculator mockSandboxPaymentStateCalculator;

    private SandboxPaymentStateUpdater mockSandboxPaymentStateUpdater;

    @Before
    public void setUp() {
        mockSandboxPaymentStateUpdater = new SandboxPaymentStateUpdater(mockPaymentUpdateService, mockSandboxPaymentStateCalculator);
    }

    @Test
    public void updatesPaymentWithStateReturnedByCalculator() {
        given(mockSandboxPaymentStateCalculator.calculate(SANDBOX_PAYMENT_ID)).willReturn(Optional.of(SUCCESS));

        mockSandboxPaymentStateUpdater.updateState(SANDBOX_PAYMENT_ID);

        verify(mockPaymentUpdateService).updateStateByProviderId(SANDBOX, SANDBOX_PAYMENT_ID, SUCCESS);
    }

    @Test
    public void updatesNothingIfCalculatorDoesNotReturnState() {
        given(mockSandboxPaymentStateCalculator.calculate(SANDBOX_PAYMENT_ID)).willReturn(Optional.empty());

        mockSandboxPaymentStateUpdater.updateState(SANDBOX_PAYMENT_ID);

        verify(mockPaymentUpdateService, never()).updateStateByProviderId(any(), any(), any());
    }

}
