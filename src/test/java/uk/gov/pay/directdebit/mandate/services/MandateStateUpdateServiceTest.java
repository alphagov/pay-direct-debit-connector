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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.pay.directdebit.mandate.model.Mandate.MandateBuilder.fromMandate;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.EXPIRED;
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
    private Mandate mandate = MandateFixture
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

        service.payerCreatedFor(mandate);

        verify(mockedDirectDebitEventService).registerPayerCreatedEventFor(mandate);
        verifyZeroInteractions(mockedMandateDao);
        assertThat(mandate.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void payerEditedFor_shouldRegisterAPayerEditedEvent() {
        service.payerEditedFor(mandate);

        verify(mockedDirectDebitEventService).registerPayerEditedEventFor(mandate);
        verifyZeroInteractions(mockedMandateDao);
        assertThat(mandate.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void shouldRegisterEventWhenReceivingDirectDebitDetails() {
        service.receiveDirectDebitDetailsFor(mandate);
        verify(mockedDirectDebitEventService).registerDirectDebitReceivedEventFor(mandate);
        assertThat(mandate.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void shouldRegisterEventWhenExchangingTokens() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(CREATED)
                .toEntity();
        Mandate expectedMandateWithAwaitingDetailsState = fromMandate(mandate)
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .build();

        service.tokenExchangedFor(mandate);

        verify(mockedDirectDebitEventService).registerTokenExchangedEventFor(expectedMandateWithAwaitingDetailsState);
    }

    @Test
    public void shouldUpdateMandateStateAndRegisterEventWhenConfirmingDirectDebitDetails_andSendEmail() {
        Mandate confirmedMandate = service.confirmedDirectDebitDetailsFor(mandate);

        assertThat(confirmedMandate, is(mandate));
        verify(mockedUserNotificationService).sendMandateCreatedEmailFor(mandate);
        verify(mockedMandateDao).updateReferenceAndPaymentProviderId(confirmedMandate);

        verify(mockedDirectDebitEventService).registerDirectDebitConfirmedEventFor(confirmedMandate);

    }

    @Test
    public void mandatePendingFor_shouldRegisterAMandatePendingEvent() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(SUBMITTED)
                .toEntity();

        service.mandatePendingFor(mandate);

        verify(mockedDirectDebitEventService).registerMandatePendingEventFor(mandate);
    }

    @Test
    public void shouldSetMandateStatusToExpired_FromCreated() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(CREATED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();
        Mandate expectedMandateWithExpiredStatus = fromMandate(mandate)
                .withState(EXPIRED)
                .build();

        service.mandateExpiredFor(mandate);

        verify(mockedDirectDebitEventService).registerMandateExpiredEventFor(expectedMandateWithExpiredStatus);
        verify(mockedMandateDao).updateState(mandate.getId(), MandateState.EXPIRED);
    }

    @Test
    public void shouldSetMandateStatusToExpired_FromSubmitted() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(SUBMITTED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();

        Mandate expectedMandateWithExpiredStatus = fromMandate(mandate)
                .withState(EXPIRED)
                .build();

        service.mandateExpiredFor(mandate);

        verify(mockedDirectDebitEventService).registerMandateExpiredEventFor(expectedMandateWithExpiredStatus);
        verify(mockedMandateDao).updateState(mandate.getId(), MandateState.EXPIRED);
    }

    @Test
    public void shouldSetMandateStatusToExpired_FromDdDetails() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();

        Mandate expectedMandateWithExpiredState = fromMandate(mandate)
                .withState(EXPIRED)
                .build();

        service.mandateExpiredFor(mandate);

        verify(mockedDirectDebitEventService).registerMandateExpiredEventFor(expectedMandateWithExpiredState);
        verify(mockedMandateDao).updateState(mandate.getId(), MandateState.EXPIRED);
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
