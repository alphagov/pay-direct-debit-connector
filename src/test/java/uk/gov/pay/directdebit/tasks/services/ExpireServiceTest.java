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
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdateService;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED_TO_PROVIDER;


@RunWith(MockitoJUnitRunner.class)
public class ExpireServiceTest {

    @DropwizardTestContext
    private TestContext testContext;
    
    private MandateStatesGraph mandateStatesGraph = new MandateStatesGraph();

    @Mock
    private MandateQueryService mandateQueryService;
    
    @Mock
    private MandateStateUpdateService mandateStateUpdateService;
    
    private ExpireService expireService;
    
    @Before
    public void setup() {
        expireService = new ExpireService(mandateStatesGraph, mandateQueryService, mandateStateUpdateService);
    }

    @Test
    public void expireMandates_shouldCallMandateServiceWithPriorStatesToPending() {
        Mandate mandate = MandateFixture.aMandateFixture().withState(CREATED).toEntity();
        when(mandateQueryService
                .findAllMandatesBySetOfStatesAndMaxCreationTime(eq(mandateStatesGraph.getPriorStates(SUBMITTED_TO_PROVIDER)), any()))
                .thenReturn(Collections.singletonList(mandate));
        int numberOfExpiredMandates = expireService.expireMandates();
        assertEquals(1, numberOfExpiredMandates);
    }

    @Test
    public void shouldGetCorrectMandateStatesPriorToPending() {
        var paymentStates = mandateStatesGraph.getPriorStates(SUBMITTED_TO_PROVIDER);
        var expectedPaymentStates = Set.of(CREATED, AWAITING_DIRECT_DEBIT_DETAILS);
        assertEquals(expectedPaymentStates, paymentStates);
    }
}
