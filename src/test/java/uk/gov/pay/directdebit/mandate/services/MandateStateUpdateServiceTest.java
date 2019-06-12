package uk.gov.pay.directdebit.mandate.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;
import uk.gov.pay.directdebit.payments.services.DirectDebitEventService;

import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.pay.directdebit.mandate.model.MandateState.ACTIVE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.PENDING;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED;

@RunWith(MockitoJUnitRunner.class)
public class MandateStateUpdateServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private MandateDao mockedMandateDao;
    @Mock
    private DirectDebitEventService mockedDirectDebitEventService;
    @Mock
    private UserNotificationService mockedUserNotificationService;
    private MandateStateUpdateService service;
    private Mandate onDemandMandate = MandateFixture
            .aMandateFixture()
            .withState(AWAITING_DIRECT_DEBIT_DETAILS)
            .toEntity();

    @Before
    public void setUp() {
        service = new MandateStateUpdateService(mockedMandateDao, mockedDirectDebitEventService, mockedUserNotificationService);
    }

    @Test
    public void shouldUpdateMandateStateRegisterEventAndSendEmail_whenMandateFails() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(PENDING)
                .toEntity();
        service.mandateFailedFor(mandate);
        verify(mockedDirectDebitEventService).registerMandateFailedEventFor(mandate);
        verify(mockedUserNotificationService).sendMandateFailedEmailFor(mandate);
    }

    @Test
    public void shouldUpdateMandateStateRegisterEventAndSendEmail_whenMandateIsCancelled() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(PENDING)
                .toEntity();
        service.mandateCancelledFor(mandate);
        verify(mockedDirectDebitEventService).registerMandateCancelledEventFor(mandate);
        verify(mockedUserNotificationService).sendMandateCancelledEmailFor(mandate);
    }

    @Test
    public void mandateActiveFor_shouldRegisterAMandateActiveEvent() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(PENDING)
                .toEntity();
        service.mandateActiveFor(mandate);

        verify(mockedDirectDebitEventService).registerMandateActiveEventFor(mandate);
        assertThat(mandate.getState(), is(ACTIVE));
    }

    @Test
    public void mandateCreatedFor_shouldRegisterAMandateCreatedEvent() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(CREATED)
                .toEntity();
        service.mandateCreatedFor(mandate);
        
        verify(mockedDirectDebitEventService).registerMandateCreatedEventFor(mandate);
        assertThat(mandate.getState(), is(CREATED));
    }
    
    @Test
    public void payerCreatedFor_shouldRegisterAPayerCreatedEvent() {

        service.payerCreatedFor(onDemandMandate);

        verify(mockedDirectDebitEventService).registerPayerCreatedEventFor(onDemandMandate);
        verifyZeroInteractions(mockedMandateDao);
        assertThat(onDemandMandate.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void payerEditedFor_shouldRegisterAPayerEditedEvent() {
        service.payerEditedFor(onDemandMandate);

        verify(mockedDirectDebitEventService).registerPayerEditedEventFor(onDemandMandate);
        verifyZeroInteractions(mockedMandateDao);
        assertThat(onDemandMandate.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void shouldRegisterEventWhenReceivingDirectDebitDetails() {
        service.receiveDirectDebitDetailsFor(onDemandMandate);
        verify(mockedDirectDebitEventService).registerDirectDebitReceivedEventFor(onDemandMandate);
        assertThat(onDemandMandate.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void shouldRegisterEventWhenExchangingTokens() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(CREATED)
                .toEntity();
        service.tokenExchangedFor(mandate);
        verify(mockedDirectDebitEventService).registerTokenExchangedEventFor(mandate);
        assertThat(mandate.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void shouldUpdateMandateStateAndRegisterEventWhenConfirmingDirectDebitDetails_andSendEmail_IfOnDemand() {
        Mandate confirmedMandate = service.confirmedOnDemandDirectDebitDetailsFor(onDemandMandate);

        assertThat(confirmedMandate, is(onDemandMandate));
        verify(mockedUserNotificationService).sendOnDemandMandateCreatedEmailFor(onDemandMandate);
        verify(mockedMandateDao).updateReferenceAndPaymentProviderId(confirmedMandate);

    }

    @Test
    public void mandatePendingFor_shouldRegisterAMandatePendingEvent() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(SUBMITTED)
                .toEntity();

        service.mandatePendingFor(mandate);

        verify(mockedDirectDebitEventService).registerMandatePendingEventFor(mandate);
        assertThat(mandate.getState(), is(PENDING));
    }

    @Test
    public void shouldThrowWhenSubmittingTheSameDetailsSeveralTimes_andCreateOnlyOneEvent_whenConfirmingDirectDebitDetails() {

        // first call with confirmation details
        service.confirmedOnDemandDirectDebitDetailsFor(onDemandMandate);

        thrown.expect(InvalidStateTransitionException.class);
        thrown.expectMessage("Transition DIRECT_DEBIT_DETAILS_CONFIRMED from state SUBMITTED is not valid");
        // second call with same details that should throw and prevent adding a new event
        Mandate newMandate = service.confirmedOnDemandDirectDebitDetailsFor(onDemandMandate);

        assertThat(newMandate.getState(), is(SUBMITTED));
        verify(mockedDirectDebitEventService, times(1)).registerDirectDebitConfirmedEventFor(newMandate);
    }

    @Test
    public void shouldSetMandateStatusToExpired_FromCreated() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(CREATED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();
        service.mandateExpiredFor(mandate);
        verify(mockedDirectDebitEventService).registerMandateExpiredEventFor(mandate);
        verify(mockedMandateDao).updateState(mandate.getId(), MandateState.EXPIRED);
        assertThat(mandate.getState(), is(MandateState.EXPIRED));
    }

    @Test
    public void shouldSetMandateStatusToExpired_FromSubmitted() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(SUBMITTED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();
        service.mandateExpiredFor(mandate);
        verify(mockedDirectDebitEventService).registerMandateExpiredEventFor(mandate);
        verify(mockedMandateDao).updateState(mandate.getId(), MandateState.EXPIRED);
        assertThat(mandate.getState(), is(MandateState.EXPIRED));
    }

    @Test
    public void shouldSetMandateStatusToExpired_FromDdDetails() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();
        service.mandateExpiredFor(mandate);
        verify(mockedDirectDebitEventService).registerMandateExpiredEventFor(mandate);
        verify(mockedMandateDao).updateState(mandate.getId(), MandateState.EXPIRED);
        assertThat(mandate.getState(), is(MandateState.EXPIRED));
    }

    @Test(expected = InvalidStateTransitionException.class)
    public void shouldNotExpireMandateSinceWrongState_PENDING() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(PENDING)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();
        service.mandateExpiredFor(mandate);
    }
}
