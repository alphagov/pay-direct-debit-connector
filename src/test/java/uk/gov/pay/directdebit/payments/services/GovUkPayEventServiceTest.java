package uk.gov.pay.directdebit.payments.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.exception.InvalidGovUkPayEventInsertionException;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventStateGraph;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdater;
import uk.gov.pay.directdebit.payments.model.Payment;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.ResourceType.MANDATE;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.ResourceType.PAYMENT;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_EXPIRED_BY_SYSTEM;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_SUBMITTED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GovUkPayEventFixture.aGovUkPayEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

@RunWith(MockitoJUnitRunner.class)
public class GovUkPayEventServiceTest {

    @Mock
    private GovUkPayEventDao mockGovUkPayEventDao;

    @Mock
    private GovUkPayEventStateGraph mockGovUkPayEventStateGraph;
    
    @Mock
    private MandateStateUpdater mockMandateStateUpdater;

    @InjectMocks
    private GovUkPayEventService govUkPayEventService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Captor
    private ArgumentCaptor<GovUkPayEvent> eventCaptor;

    private Long mandateId = 1L;
    private Mandate mandate;

    private Long paymentId = 2L;
    private Payment payment;

    @Before
    public void setUp() {
        mandate = aMandateFixture().withId(mandateId).toEntity();
        payment = aPaymentFixture().withId(paymentId).toEntity();
    }

    @Test
    public void insertMandateEvent_insertedForValidTransition() {
        var previousEvent = aGovUkPayEventFixture()
                .withResourceType(MANDATE)
                .withMandateId(mandateId)
                .withEventType(MANDATE_CREATED)
                .toEntity();
        GovUkPayEventType newEventType = MANDATE_EXPIRED_BY_SYSTEM;

        when(mockGovUkPayEventDao.findLatestEventForMandate(mandateId)).thenReturn(Optional.of(previousEvent));
        when(mockGovUkPayEventStateGraph.isValidTransition(previousEvent.getEventType(), newEventType)).thenReturn(true);

        govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, newEventType);

        verify(mockGovUkPayEventDao).insert(eventCaptor.capture());
        verify(mockMandateStateUpdater).updateStateIfNecessary(mandate);

        GovUkPayEvent insertedEvent = eventCaptor.getValue();
        assertThat(insertedEvent.getEventType(), is(newEventType));
        assertThat(insertedEvent.getResourceType(), is(MANDATE));
        assertThat(insertedEvent.getMandateId(), is(Optional.of(mandateId)));
        assertThat(insertedEvent.getPaymentId(), is(Optional.empty()));
        assertThat(insertedEvent.getEventDate(), is(notNullValue()));
    }

    @Test
    public void insertMandateEvent_insertedForValidInitialEvent() {
        GovUkPayEventType eventType = MANDATE_CREATED;

        when(mockGovUkPayEventDao.findLatestEventForMandate(mandateId)).thenReturn(Optional.empty());
        when(mockGovUkPayEventStateGraph.isValidStartValue(eventType)).thenReturn(true);

        govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, eventType);

        verify(mockGovUkPayEventDao).insert(eventCaptor.capture());
        verify(mockMandateStateUpdater).updateStateIfNecessary(mandate);

        GovUkPayEvent insertedEvent = eventCaptor.getValue();
        assertThat(insertedEvent.getEventType(), is(eventType));
        assertThat(insertedEvent.getResourceType(), is(MANDATE));
        assertThat(insertedEvent.getMandateId(), is(Optional.of(mandateId)));
        assertThat(insertedEvent.getPaymentId(), is(Optional.empty()));
        assertThat(insertedEvent.getEventDate(), is(notNullValue()));
    }

    @Test
    public void insertMandateEvent_shouldThrowForInvalidEventTransition() {
        var previousEvent = aGovUkPayEventFixture()
                .withResourceType(MANDATE)
                .withMandateId(mandateId)
                .withEventType(MANDATE_EXPIRED_BY_SYSTEM)
                .toEntity();
        GovUkPayEventType newEventType = MANDATE_CREATED;

        when(mockGovUkPayEventDao.findLatestEventForMandate(mandateId)).thenReturn(Optional.of(previousEvent));
        when(mockGovUkPayEventStateGraph.isValidTransition(previousEvent.getEventType(), newEventType)).thenReturn(false);

        expectedException.expect(InvalidGovUkPayEventInsertionException.class);
        expectedException.expectMessage("GOV.UK Pay event MANDATE_CREATED is invalid following event MANDATE_EXPIRED_BY_SYSTEM");

        govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, newEventType);
    }

    @Test
    public void insertMandateEvent_shouldThrowForInvalidInitialEvent() {
        GovUkPayEventType eventType = MANDATE_SUBMITTED;

        when(mockGovUkPayEventDao.findLatestEventForMandate(mandateId)).thenReturn(Optional.empty());
        when(mockGovUkPayEventStateGraph.isValidStartValue(eventType)).thenReturn(false);

        expectedException.expect(InvalidGovUkPayEventInsertionException.class);
        expectedException.expectMessage("GOV.UK Pay event MANDATE_SUBMITTED is invalid when there are no previous events");

        govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, eventType);
    }

    @Test
    public void insertPaymentEvent_insertedForValidInitialEvent() {
        GovUkPayEventType eventType = PAYMENT_SUBMITTED;

        when(mockGovUkPayEventDao.findLatestEventForPayment(paymentId)).thenReturn(Optional.empty());
        when(mockGovUkPayEventStateGraph.isValidStartValue(eventType)).thenReturn(true);

        govUkPayEventService.storeEventForPayment(payment, eventType);

        verify(mockGovUkPayEventDao).insert(eventCaptor.capture());

        GovUkPayEvent insertedEvent = eventCaptor.getValue();
        assertThat(insertedEvent.getEventType(), is(eventType));
        assertThat(insertedEvent.getResourceType(), is(PAYMENT));
        assertThat(insertedEvent.getMandateId(), is(Optional.empty()));
        assertThat(insertedEvent.getPaymentId(), is(Optional.of(paymentId)));
        assertThat(insertedEvent.getEventDate(), is(notNullValue()));
    }

    @Test
    public void insertPaymentEvent_shouldThrowForInvalidEventTransition() {
        var previousEvent = aGovUkPayEventFixture()
                .withResourceType(GovUkPayEvent.ResourceType.PAYMENT)
                .withPaymentId(paymentId)
                .withEventType(PAYMENT_SUBMITTED)
                .toEntity();
        GovUkPayEventType newEventType = PAYMENT_SUBMITTED;

        when(mockGovUkPayEventDao.findLatestEventForPayment(paymentId)).thenReturn(Optional.of(previousEvent));
        when(mockGovUkPayEventStateGraph.isValidTransition(previousEvent.getEventType(), newEventType)).thenReturn(false);

        expectedException.expect(InvalidGovUkPayEventInsertionException.class);
        expectedException.expectMessage("GOV.UK Pay event PAYMENT_SUBMITTED is invalid following event PAYMENT_SUBMITTED");

        govUkPayEventService.storeEventForPayment(payment, newEventType);
    }
}
