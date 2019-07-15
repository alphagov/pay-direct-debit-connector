package uk.gov.pay.directdebit.mandate.services.gocardless;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.services.MandateUpdateService;
import uk.gov.pay.directdebit.payments.model.GoCardlessMandateIdAndOrganisationId;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessMandateStateUpdaterTest {

    private static final GoCardlessMandateIdAndOrganisationId GOCARDLESS_MANDATE_ID_AND_ORGANISATION_ID = new GoCardlessMandateIdAndOrganisationId(
            GoCardlessMandateId.valueOf("MD123"), GoCardlessOrganisationId.valueOf("OR123"));

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
        given(mockGoCardlessMandateStateCalculator.calculate(GOCARDLESS_MANDATE_ID_AND_ORGANISATION_ID))
                .willReturn(Optional.of(mockMandateStateWithDetails));

        mockGoCardlessMandateStateUpdater.updateState(GOCARDLESS_MANDATE_ID_AND_ORGANISATION_ID);

        verify(mockMandateUpdateService).updateStateByPaymentProviderMandateId(GOCARDLESS, GOCARDLESS_MANDATE_ID_AND_ORGANISATION_ID,
                mockMandateStateWithDetails);
    }

    @Test
    public void updatesNothingIfCalculatorDoesNotReturnState() {
        given(mockGoCardlessMandateStateCalculator.calculate(GOCARDLESS_MANDATE_ID_AND_ORGANISATION_ID)).willReturn(Optional.empty());

        mockGoCardlessMandateStateUpdater.updateState(GOCARDLESS_MANDATE_ID_AND_ORGANISATION_ID);

        verify(mockMandateUpdateService, never()).updateStateByPaymentProviderMandateId(any(), any(), any());
    }

}
