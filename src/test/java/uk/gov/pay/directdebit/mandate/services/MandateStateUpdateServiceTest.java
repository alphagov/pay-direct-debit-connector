package uk.gov.pay.directdebit.mandate.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;

import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_USER_SETUP_EXPIRED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.PENDING;

@RunWith(MockitoJUnitRunner.class)
public class MandateStateUpdateServiceTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Mock
    private MandateDao mockedMandateDao;
    
    @Mock
    private UserNotificationService mockedUserNotificationService;
    
    @Mock
    private GovUkPayEventService mockedGovUkPayEventService;
    
    @InjectMocks
    private MandateStateUpdateService service;
    
    private Mandate mandate = MandateFixture
            .aMandateFixture()
            .withState(AWAITING_DIRECT_DEBIT_DETAILS)
            .toEntity();

    @Test
    public void shouldSendEmailWhenMandateFails() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(PENDING)
                .toEntity();

        service.mandateFailedFor(mandate);

        verify(mockedUserNotificationService).sendMandateFailedEmailFor(mandate);
    }

    @Test
    public void shouldSendEmailWhenMandateIsCancelled() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(PENDING)
                .toEntity();

        service.mandateCancelledFor(mandate);

        verify(mockedUserNotificationService).sendMandateCancelledEmailFor(mandate);
    }

    @Test
    public void shouldRegisterEventWhenMandateCreated() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(CREATED)
                .toEntity();
        service.mandateCreatedFor(mandate);
        
        verify(mockedGovUkPayEventService).storeEventAndUpdateStateForMandate(mandate, MANDATE_CREATED);
    }

    @Test
    public void shouldRegisterEventWhenExchangingTokens() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(CREATED)
                .toEntity();

        service.tokenExchangedFor(mandate);

        verify(mockedGovUkPayEventService).storeEventAndUpdateStateForMandate(mandate, MANDATE_TOKEN_EXCHANGED);
    }

    @Test
    public void shouldRegisterEventAndSendEmailWhenConfirmingDirectDebitDetails() {
        service.confirmedDirectDebitDetailsFor(mandate);

        verify(mockedUserNotificationService).sendMandateCreatedEmailFor(mandate);
        verify(mockedMandateDao).updateReferenceAndPaymentProviderId(mandate);

        verify(mockedGovUkPayEventService).storeEventAndUpdateStateForMandate(mandate, MANDATE_SUBMITTED_TO_PROVIDER);
    }

    @Test
    public void shouldRegisterEventForMandateExpired() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(CREATED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();

        service.mandateExpiredFor(mandate);

        verify(mockedGovUkPayEventService).storeEventAndUpdateStateForMandate(mandate, MANDATE_USER_SETUP_EXPIRED);
    }
}
