package uk.gov.pay.directdebit.tasks.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;


@RunWith(MockitoJUnitRunner.class)
public class ExpireServiceTest {

    @DropwizardTestContext
    private TestContext testContext;

    @Mock
    private MandateQueryService mockMandateQueryService;

    @Mock
    private GovUkPayEventService mockGovUkPayEventService;

    @InjectMocks
    private ExpireService expireService;

    @Test
    public void expireMandates_shouldCallMandateServiceWithPriorStatesToPending() {
        Mandate mandate = MandateFixture.aMandateFixture().withState(CREATED).toEntity();
        when(mockMandateQueryService
                .findAllMandatesBySetOfStatesAndMaxCreationTime(any(), any()))
                .thenReturn(Collections.singletonList(mandate));
        int numberOfExpiredMandates = expireService.expireMandates();

        verify(mockGovUkPayEventService).storeEventAndUpdateStateForMandate(mandate, GovUkPayEventType.MANDATE_USER_SETUP_EXPIRED);
        assertEquals(1, numberOfExpiredMandates);
    }
}
