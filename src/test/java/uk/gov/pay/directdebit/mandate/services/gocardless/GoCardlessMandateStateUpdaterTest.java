package uk.gov.pay.directdebit.mandate.services.gocardless;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.PENDING;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessMandateStateUpdaterTest {

    private static final GoCardlessMandateId GOCARDLESS_MANDATE_ID = GoCardlessMandateId.valueOf("MD123");
    private static final GoCardlessOrganisationId GOCARDLESS_ORGANISATION_ID = GoCardlessOrganisationId.valueOf("OR123");

    @Mock
    private MandateDao mockMandateDao;

    @Mock
    private GoCardlessMandateStateCalculator mockGoCardlessMandateStateCalculator;

    private GoCardlessMandateStateUpdater mockGoCardlessMandateStateUpdater;

    @Before
    public void setUp() {
        mockGoCardlessMandateStateUpdater = new GoCardlessMandateStateUpdater(mockMandateDao, mockGoCardlessMandateStateCalculator);
    }

    @Test
    public void updatesMandateWithStateReturnedByCalculator() {
        given(mockGoCardlessMandateStateCalculator.calculate(GOCARDLESS_MANDATE_ID, GOCARDLESS_ORGANISATION_ID))
                .willReturn(Optional.of(PENDING));

        mockGoCardlessMandateStateUpdater.updateState(GOCARDLESS_MANDATE_ID, GOCARDLESS_ORGANISATION_ID);

        verify(mockMandateDao).updateStateByPaymentProviderMandateId(GOCARDLESS, GOCARDLESS_ORGANISATION_ID, GOCARDLESS_MANDATE_ID, PENDING);
    }

    @Test
    public void updatesNothingIfCalculatorDoesNotReturnState() {
        given(mockGoCardlessMandateStateCalculator.calculate(GOCARDLESS_MANDATE_ID, GOCARDLESS_ORGANISATION_ID))
                .willReturn(Optional.empty());

        mockGoCardlessMandateStateUpdater.updateState(GOCARDLESS_MANDATE_ID, GOCARDLESS_ORGANISATION_ID);

        verify(mockMandateDao, never()).updateStateByPaymentProviderMandateId(any(), any(), any(), any());
    }

}
