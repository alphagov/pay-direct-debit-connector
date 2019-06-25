package uk.gov.pay.directdebit.payments.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;

import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessEventServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Mock
    private GoCardlessEventDao mockedGoCardlessEventDao;

    private GoCardlessEventService service;

    @Before
    public void setUp() {
        service = new GoCardlessEventService(mockedGoCardlessEventDao);
    }  
    
    @Test
    public void storeEvent_shouldStoreAGoCardlessEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().toEntity();
        service.storeEvent(goCardlessEvent);
        verify(mockedGoCardlessEventDao).insert(goCardlessEvent);
    }
}
