package uk.gov.pay.directdebit.tasks.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdateService;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;


@RunWith(MockitoJUnitRunner.class)
public class ExpireServiceTest {

    @DropwizardTestContext
    private TestContext testContext;

    @Mock
    private MandateQueryService mandateQueryService;
    
    @Mock
    private MandateStateUpdateService mandateStateUpdateService;
    
    private ExpireService expireService;
    
    @Before
    public void setup() {
        expireService = new ExpireService(mandateQueryService, mandateStateUpdateService);
    }

    @Test
    public void expireMandates_shouldCallMandateServiceWithPriorStatesToPending() {
        Mandate mandate = MandateFixture.aMandateFixture().withState(CREATED).toEntity();
        when(mandateQueryService
                .findAllMandatesBySetOfStatesAndMaxCreationTime(any(), any()))
                .thenReturn(Collections.singletonList(mandate));
        int numberOfExpiredMandates = expireService.expireMandates();
        assertEquals(1, numberOfExpiredMandates);
    }
}
