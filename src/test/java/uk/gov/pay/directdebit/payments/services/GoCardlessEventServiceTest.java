package uk.gov.pay.directdebit.payments.services;

import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotFoundException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessPaymentNotFoundException;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessEventServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Mock
    private GoCardlessEventDao mockedGoCardlessEventDao;
    @Mock
    private GoCardlessMandateDao mockedGoCardlessMandateDao;
    @Mock
    private GoCardlessPaymentDao mockedGoCardlessPaymentDao;
    
    private GoCardlessEventService service;

    @Before
    public void setUp() {
        service = new GoCardlessEventService(mockedGoCardlessPaymentDao, mockedGoCardlessMandateDao, mockedGoCardlessEventDao);
    }  
    
    @Test
    public void storeEvent_shouldStoreAGoCardlessEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().toEntity();
        service.storeEvent(goCardlessEvent);
        verify(mockedGoCardlessEventDao).insert(goCardlessEvent);
    }

    @Test
    public void findPaymentForEvent_shouldThrowIfNoPaymentIsFoundForEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().toEntity();
        String resourceId = "aaa";
        when(mockedGoCardlessPaymentDao.findByEventResourceId(resourceId)).thenReturn(Optional.empty());
        thrown.expect(GoCardlessPaymentNotFoundException.class);
        thrown.expectMessage("No gocardless payment found with resource id: aaa");
        thrown.reportMissingExceptionWithMessage("GoCardlessPaymentNotFoundException expected");
        service.findPaymentForEvent(goCardlessEvent);
    }

    @Test
    public void findMandateForEvent_shouldThrowIfNoMandateIsFoundForEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().toEntity();
        String resourceId = "aaa";
        when(mockedGoCardlessMandateDao.findByEventResourceId(resourceId)).thenReturn(Optional.empty());
        thrown.expect(GoCardlessMandateNotFoundException.class);
        thrown.expectMessage("No gocardless mandate found with resource id: aaa");
        thrown.reportMissingExceptionWithMessage("GoCardlessMandateNotFoundException expected");
        service.findGoCardlessMandateForEvent(goCardlessEvent);
    }
}
