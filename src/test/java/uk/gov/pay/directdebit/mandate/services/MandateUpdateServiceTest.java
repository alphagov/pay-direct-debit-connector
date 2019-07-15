package uk.gov.pay.directdebit.mandate.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.MandateLookupKey;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.payments.model.GoCardlessMandateIdAndOrganisationId;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.mandate.model.MandateState.PENDING;

@RunWith(MockitoJUnitRunner.class)
public class MandateUpdateServiceTest {
    
    private static final SandboxMandateId SANDBOX_MANDATE_ID = SandboxMandateId.valueOf("Sandy");
    private static final GoCardlessMandateId GOCARDLESS_MANDATE_ID = GoCardlessMandateId.valueOf("MD123");
    private static final GoCardlessOrganisationId GOCARDLESS_ORGANISATION_ID = GoCardlessOrganisationId.valueOf("OR123");
    private static final GoCardlessMandateIdAndOrganisationId GOCARDLESS_MANDATE_ID_AND_ORGANISATION_ID =
            new GoCardlessMandateIdAndOrganisationId(GOCARDLESS_MANDATE_ID, GOCARDLESS_ORGANISATION_ID);
    
    private static final String MANDATE_STATE_DETAILS = "state details";
    private static final String MANDATE_STATE_DETAILS_DESCRIPTION = "state details description";
    private static final DirectDebitStateWithDetails<MandateState> MANDATE_STATE_WITH_DETAILS = new DirectDebitStateWithDetails<>(PENDING,
            MANDATE_STATE_DETAILS, MANDATE_STATE_DETAILS_DESCRIPTION);

    @Mock
    private MandateDao mockMandateDao;
    
    private MandateUpdateService mandateUpdateService;
    
    @Before
    public void setUp() {
        mandateUpdateService = new MandateUpdateService(mockMandateDao);
    }

    @Test
    public void updateStateByPaymentProviderMandateIdWithSandboxMandateIdReturnsUpdateCount() {
        given(mockMandateDao.updateStateByProviderId(SANDBOX, SANDBOX_MANDATE_ID, PENDING, MANDATE_STATE_DETAILS, MANDATE_STATE_DETAILS_DESCRIPTION)).willReturn(1);

        int updated = mandateUpdateService.updateStateByPaymentProviderMandateId(SANDBOX, SANDBOX_MANDATE_ID, MANDATE_STATE_WITH_DETAILS);

        assertThat(updated, is(1));

        verifyZeroInteractions(ignoreStubs(mockMandateDao));
    }

    @Test
    public void updateStateByPaymentProviderMandateIdWithGoCardlessMandateIdAndOrganisationIdReturnsUpdateCount() {
        given(mockMandateDao.updateStateByProviderIdAndOrganisationId(GOCARDLESS, GOCARDLESS_ORGANISATION_ID, GOCARDLESS_MANDATE_ID,
                PENDING, MANDATE_STATE_DETAILS, MANDATE_STATE_DETAILS_DESCRIPTION)).willReturn(1);

        int updated = mandateUpdateService.updateStateByPaymentProviderMandateId(GOCARDLESS, GOCARDLESS_MANDATE_ID_AND_ORGANISATION_ID,
                MANDATE_STATE_WITH_DETAILS);

        assertThat(updated, is(1));

        verifyZeroInteractions(ignoreStubs(mockMandateDao));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateStateByPaymentProviderMandateIdWithUnrecognisedTypeThrowsException() {
        
        mandateUpdateService.updateStateByPaymentProviderMandateId(GOCARDLESS, new UnrecognisedMandateLookupKeyImplementation(),
                MANDATE_STATE_WITH_DETAILS);
    }
    
    private static class UnrecognisedMandateLookupKeyImplementation implements MandateLookupKey {
        
    }

}
