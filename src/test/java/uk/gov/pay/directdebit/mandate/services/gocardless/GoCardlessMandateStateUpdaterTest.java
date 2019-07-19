package uk.gov.pay.directdebit.mandate.services.gocardless;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.services.MandateUpdateService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessMandateStateUpdaterTest {
    private static final Mandate mandate = aMandateFixture().toEntity();

    @Mock
    private DirectDebitStateWithDetails<MandateState> mockMandateStateWithDetails;

    @Mock
    private MandateUpdateService mockMandateUpdateService;

    @Mock
    private GoCardlessMandateStateCalculator mockGoCardlessMandateStateCalculator;

    private GoCardlessMandateStateUpdater mockGoCardlessMandateStateUpdater;

    @Before
    public void setUp() {
        mockGoCardlessMandateStateUpdater = new GoCardlessMandateStateUpdater(mockMandateUpdateService, mockGoCardlessMandateStateCalculator);
    }

    @Test
    public void updatesMandateWithStateReturnedByCalculator() {
        given(mockGoCardlessMandateStateCalculator.calculate(mandate))
                .willReturn(Optional.of(mockMandateStateWithDetails));

        mockGoCardlessMandateStateUpdater.updateState(mandate);

        verify(mockMandateUpdateService).updateState(mandate,
                mockMandateStateWithDetails);
    }

    @Test
    public void updatesNothingIfCalculatorDoesNotReturnState() {
        given(mockGoCardlessMandateStateCalculator.calculate(mandate)).willReturn(Optional.empty());

        mockGoCardlessMandateStateUpdater.updateState(mandate);

        verify(mockMandateUpdateService, never()).updateState(any(), any());
    }

}
