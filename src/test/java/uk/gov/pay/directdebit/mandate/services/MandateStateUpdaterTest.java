package uk.gov.pay.directdebit.mandate.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessMandateStateCalculator;
import uk.gov.pay.directdebit.mandate.services.sandbox.SandboxMandateStateCalculator;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class MandateStateUpdaterTest {
    @Mock
    private DirectDebitStateWithDetails<MandateState> mockMandateStateWithDetails;

    @Mock
    private MandateUpdateService mockMandateUpdateService;

    @Mock
    private GoCardlessMandateStateCalculator mockGoCardlessMandateStateCalculator;

    @Mock
    private SandboxMandateStateCalculator mockSandboxStateCalculator;

    @InjectMocks
    private MandateStateUpdater mockMandateStateUpdater;

    @Test
    public void updatesMandateWithStateReturnedByCalculatorForGoCardlessMandate() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().withPaymentProvider(GOCARDLESS);
        Mandate mandate = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).toEntity();

        given(mockGoCardlessMandateStateCalculator.calculate(mandate))
                .willReturn(Optional.of(mockMandateStateWithDetails));

        mockMandateStateUpdater.updateState(mandate);

        verify(mockMandateUpdateService).updateState(mandate, mockMandateStateWithDetails);
    }

    @Test
    public void updatesMandateWithStateReturnedByCalculatorForSandboxMandate() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().withPaymentProvider(SANDBOX);
        Mandate mandate = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).toEntity();

        given(mockSandboxStateCalculator.calculate(mandate))
                .willReturn(Optional.of(mockMandateStateWithDetails));

        mockMandateStateUpdater.updateState(mandate);
        
        verify(mockMandateUpdateService).updateState(mandate, mockMandateStateWithDetails);
    }

    @Test
    public void updatesNothingIfCalculatorDoesNotReturnState() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().withPaymentProvider(GOCARDLESS);
        Mandate mandate = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).toEntity();

        given(mockGoCardlessMandateStateCalculator.calculate(mandate)).willReturn(Optional.empty());

        mockMandateStateUpdater.updateState(mandate);

        verify(mockMandateUpdateService, never()).updateState(any(), any());
    }

}
