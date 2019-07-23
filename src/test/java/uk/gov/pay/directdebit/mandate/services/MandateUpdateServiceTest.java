package uk.gov.pay.directdebit.mandate.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;

@RunWith(MockitoJUnitRunner.class)
public class MandateUpdateServiceTest {
    
    @Mock
    MandateDao mockMandateDao;
    
    @InjectMocks
    MandateUpdateService mandateUpdateService;
    
    private Mandate mandate = aMandateFixture().toEntity();
    
    @Test
    public void callsToUpdateStateAndReturnsUpdatedMandate_whenDetailsAndDescriptionAreEmpty() {
        MandateState state = MandateState.PENDING;
        DirectDebitStateWithDetails<MandateState> stateWithDetails = new DirectDebitStateWithDetails<>(state);

        Mandate updatedMandate = mandateUpdateService.updateState(mandate, stateWithDetails);

        verify(mockMandateDao).updateStateAndDetails(mandate.getId(), state, null, null);
        
        assertThat(updatedMandate.getExternalId(), is(mandate.getExternalId()));
        assertThat(updatedMandate.getState(), is(state));
        assertThat(updatedMandate.getStateDetails(), is(Optional.empty()));
        assertThat(updatedMandate.getStateDetailsDescription(), is(Optional.empty()));
    }

    @Test
    public void callsToUpdateStateAndReturnsUpdatedMandate_withDetailsAndDescription() {
        MandateState state = MandateState.PENDING;
        String details = "a-details";
        String description = "a-description";
        DirectDebitStateWithDetails<MandateState> stateWithDetails = new DirectDebitStateWithDetails<>(state, details, description);

        Mandate updatedMandate = mandateUpdateService.updateState(mandate, stateWithDetails);

        verify(mockMandateDao).updateStateAndDetails(mandate.getId(), state, details, description);

        assertThat(updatedMandate.getExternalId(), is(mandate.getExternalId()));
        assertThat(updatedMandate.getState(), is(state));
        assertThat(updatedMandate.getStateDetails(), is(Optional.of(details)));
        assertThat(updatedMandate.getStateDetailsDescription(), is(Optional.of(description)));
    }
}
