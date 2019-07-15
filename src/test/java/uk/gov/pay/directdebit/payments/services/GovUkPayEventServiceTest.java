package uk.gov.pay.directdebit.payments.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.exception.GovUkPayEventHasNoMandateIdException;
import uk.gov.pay.directdebit.events.exception.GovUkPayEventHasNoPaymentIdException;
import uk.gov.pay.directdebit.events.exception.InvalidGovUkPayEventInsertionException;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventStateGraph;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.model.Payment;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_EXPIRED_BY_SYSTEM;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_SUBMITTED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GovUkPayEventFixture.aGovUkPayEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

@RunWith(MockitoJUnitRunner.class)
public class GovUkPayEventServiceTest {
    
    @Mock
    private GovUkPayEventDao govUkPayEventDao;
    
    @Mock
    private GovUkPayEventStateGraph govUkPayEventStateGraph;

    @InjectMocks
    private GovUkPayEventService govUkPayEventService;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Long mandateId = 1L;
    private Mandate mandate;
    
    private Long paymentId = 2L;
    private Payment payment;

    @Test
    public void insertMandateEvent_insertedForValidTransition() {
        var previousEvent = aGovUkPayEventFixture()
                .withResourceType(GovUkPayEvent.ResourceType.MANDATE)
                .withMandateId(mandateId)
                .withEventType(MANDATE_CREATED)
                .toEntity();
        var newEvent = aGovUkPayEventFixture()
                .withMandateId(mandateId)
                .withEventType(MANDATE_EXPIRED_BY_SYSTEM)
                .toEntity();

        when(govUkPayEventDao.findLatestEventForMandate(mandateId)).thenReturn(Optional.of(previousEvent));
        when(govUkPayEventStateGraph.isValidTransition(previousEvent.getEventType(), newEvent.getEventType())).thenReturn(true);

        govUkPayEventService.storeEvent(newEvent);
        
        verify(govUkPayEventDao).insert(newEvent);
    }

    @Test
    public void insertMandateEvent_insertedForValidInitialEvent() {
        var newEvent = aGovUkPayEventFixture()
                .withResourceType(GovUkPayEvent.ResourceType.MANDATE)
                .withMandateId(mandateId)
                .withEventType(MANDATE_CREATED)
                .toEntity();

        when(govUkPayEventDao.findLatestEventForMandate(mandateId)).thenReturn(Optional.empty());
        when(govUkPayEventStateGraph.isValidStartValue(newEvent.getEventType())).thenReturn(true);

        govUkPayEventService.storeEvent(newEvent);

        verify(govUkPayEventDao).insert(newEvent);
    }

    @Test
    public void insertMandateEvent_shouldThrowForInvalidEventTransition() {
        var previousEvent = aGovUkPayEventFixture()
                .withResourceType(GovUkPayEvent.ResourceType.MANDATE)
                .withMandateId(mandateId)
                .withEventType(MANDATE_EXPIRED_BY_SYSTEM)
                .toEntity();
        var newEvent = aGovUkPayEventFixture()
                .withResourceType(GovUkPayEvent.ResourceType.MANDATE)
                .withMandateId(mandateId)
                .withEventType(MANDATE_CREATED)
                .toEntity();
        
        when(govUkPayEventDao.findLatestEventForMandate(mandateId)).thenReturn(Optional.of(previousEvent));
        when(govUkPayEventStateGraph.isValidTransition(previousEvent.getEventType(), newEvent.getEventType())).thenReturn(false);
        
        expectedException.expect(InvalidGovUkPayEventInsertionException.class);
        expectedException.expectMessage("GOV.UK Pay event MANDATE_CREATED is invalid following event MANDATE_EXPIRED_BY_SYSTEM");
        
        govUkPayEventService.storeEvent(newEvent);
    }

    @Test
    public void insertMandateEvent_shouldThrowForInvalidInitialEvent() {
        var newEvent = aGovUkPayEventFixture()
                .withResourceType(GovUkPayEvent.ResourceType.MANDATE)
                .withMandateId(mandateId)
                .withEventType(MANDATE_SUBMITTED)
                .toEntity();

        when(govUkPayEventDao.findLatestEventForMandate(mandateId)).thenReturn(Optional.empty());
        when(govUkPayEventStateGraph.isValidStartValue(newEvent.getEventType())).thenReturn(false);

        expectedException.expect(InvalidGovUkPayEventInsertionException.class);
        expectedException.expectMessage("GOV.UK Pay event MANDATE_SUBMITTED is invalid when there are no previous events");

        govUkPayEventService.storeEvent(newEvent);
    }

    @Test
    public void insertMandateEvent_shouldThrowForMissingMandateId() {
        var newEvent = aGovUkPayEventFixture()
                .withResourceType(GovUkPayEvent.ResourceType.MANDATE)
                .withEventType(MANDATE_CREATED)
                .toEntity();

        expectedException.expect(GovUkPayEventHasNoMandateIdException.class);
        
        govUkPayEventService.storeEvent(newEvent);
    }

    @Test
    public void insertPaymentEvent_insertedForValidInitialEvent() {
        var newEvent = aGovUkPayEventFixture()
                .withResourceType(GovUkPayEvent.ResourceType.PAYMENT)
                .withPaymentId(paymentId)
                .withEventType(PAYMENT_SUBMITTED)
                .toEntity();

        when(govUkPayEventDao.findLatestEventForPayment(paymentId)).thenReturn(Optional.empty());
        when(govUkPayEventStateGraph.isValidStartValue(newEvent.getEventType())).thenReturn(true);

        govUkPayEventService.storeEvent(newEvent);

        verify(govUkPayEventDao).insert(newEvent);
    }

    @Test
    public void insertPaymentEvent_shouldThrowForInvalidEventTransition() {
        var previousEvent = aGovUkPayEventFixture()
                .withResourceType(GovUkPayEvent.ResourceType.PAYMENT)
                .withPaymentId(paymentId)
                .withEventType(PAYMENT_SUBMITTED)
                .toEntity();
        var newEvent = aGovUkPayEventFixture()
                .withResourceType(GovUkPayEvent.ResourceType.PAYMENT)
                .withPaymentId(paymentId)
                .withEventType(PAYMENT_SUBMITTED)
                .toEntity();

        when(govUkPayEventDao.findLatestEventForPayment(paymentId)).thenReturn(Optional.of(previousEvent));
        when(govUkPayEventStateGraph.isValidTransition(previousEvent.getEventType(), newEvent.getEventType())).thenReturn(false);

        expectedException.expect(InvalidGovUkPayEventInsertionException.class);
        expectedException.expectMessage("GOV.UK Pay event PAYMENT_SUBMITTED is invalid following event PAYMENT_SUBMITTED");

        govUkPayEventService.storeEvent(newEvent);
    }

    @Test
    public void insertPaymentEvent_shouldThrowForMissingPaymentId() {
        var newEvent = aGovUkPayEventFixture()
                .withResourceType(GovUkPayEvent.ResourceType.PAYMENT)
                .withEventType(PAYMENT_SUBMITTED)
                .toEntity();

        expectedException.expect(GovUkPayEventHasNoPaymentIdException.class);

        govUkPayEventService.storeEvent(newEvent);
    }
}
