package uk.gov.pay.directdebit.payments.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.services.gocardless.GoCardlessPaymentStateCalculator;
import uk.gov.pay.directdebit.payments.services.sandbox.SandboxPaymentStateCalculator;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

@RunWith(MockitoJUnitRunner.class)
public class PaymentStateUpdaterTest {

    @Mock
    private DirectDebitStateWithDetails<PaymentState> mockPaymentStateWithDetails;

    @Mock
    private PaymentUpdateService mockPaymentUpdateService;

    @Mock
    private GoCardlessPaymentStateCalculator mockGoCardlessPaymentStateCalculator;
    
    @Mock
    private SandboxPaymentStateCalculator mockSandboxPaymentStateCalculator;

    @InjectMocks
    private PaymentStateUpdater paymentStateUpdater;

    @Test
    public void updatesPaymentWithStateReturnedByCalculatorForGoCardlessPayment() {
        Payment payment = createPayment(GOCARDLESS);

        given(mockGoCardlessPaymentStateCalculator.calculate(payment)).willReturn(Optional.of(mockPaymentStateWithDetails));

        paymentStateUpdater.updateStateIfNecessary(payment);

        verify(mockPaymentUpdateService).updateState(payment, mockPaymentStateWithDetails);
    }

    @Test
    public void updatesPaymentWithStateReturnedByCalculatorForSandboxPayment() {
        Payment payment = createPayment(SANDBOX);

        given(mockSandboxPaymentStateCalculator.calculate(payment)).willReturn(Optional.of(mockPaymentStateWithDetails));

        paymentStateUpdater.updateStateIfNecessary(payment);

        verify(mockPaymentUpdateService).updateState(payment, mockPaymentStateWithDetails);
    }

    @Test
    public void updatesNothingIfCalculatorDoesNotReturnState() {
        Payment payment = createPayment(GOCARDLESS);
        
        given(mockGoCardlessPaymentStateCalculator.calculate(payment)).willReturn(Optional.empty());

        paymentStateUpdater.updateStateIfNecessary(payment);

        verify(mockPaymentUpdateService, never()).updateState(any(), any());
    }

    private Payment createPayment(PaymentProvider paymentProvider) {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().withPaymentProvider(paymentProvider);
        MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture);
        return aPaymentFixture().withMandateFixture(mandateFixture).toEntity();
    }

}
