package uk.gov.pay.directdebit.payments.services;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_EXPIRED_BY_SYSTEM;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.PaymentState.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private TransactionDao mockedTransactionDao;
    @Mock
    private UserNotificationService mockedUserNotificationService;
    @Mock
    private TokenService mockedTokenService;
    @Mock
    private GatewayAccountDao mockedGatewayAccountDao;
    @Mock
    private DirectDebitConfig mockedDirectDebitConfig;
    @Mock
    private CreatePaymentParser mockedCreatePaymentParser;
    private PayerFixture payerFixture = PayerFixture.aPayerFixture();
    private TransactionService service;
    @Mock
    private DirectDebitEventService mockedDirectDebitEventService;

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).withPayerFixture(payerFixture);
    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture);
    @Before
    public void setUp() {
        service = new TransactionService(mockedTokenService, mockedGatewayAccountDao, mockedDirectDebitConfig, mockedTransactionDao,
                mockedDirectDebitEventService, mockedUserNotificationService, mockedCreatePaymentParser);
    }

    @Test
    public void findByTransactionExternalIdAndAccountId_shouldFindATransaction() {
        when(mockedTransactionDao.findByExternalId(transactionFixture.getExternalId()))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        Transaction foundTransaction = service.findTransactionForExternalIdAndGatewayAccountExternalId(transactionFixture.getExternalId(), gatewayAccountFixture.getExternalId());
        assertThat(foundTransaction.getId(), is(notNullValue()));
        assertThat(foundTransaction.getExternalId(), is(transactionFixture.getExternalId()));
        assertThat(foundTransaction.getMandate(), is(mandateFixture.toEntity()));
        assertThat(foundTransaction.getState(), is(transactionFixture.getState()));
        assertThat(foundTransaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(foundTransaction.getDescription(), is(transactionFixture.getDescription()));
        assertThat(foundTransaction.getReference(), is(transactionFixture.getReference()));
        assertThat(foundTransaction.getCreatedDate(), is(transactionFixture.getCreatedDate()));
    }

    @Test
    public void findChargeForExternalIdAndGatewayAccountId_shouldThrow_ifNoTransactionExistsWithExternalId() {
        thrown.expect(ChargeNotFoundException.class);
        thrown.expectMessage("No charges found for transaction id: not-existing");
        thrown.reportMissingExceptionWithMessage("ChargeNotFoundException expected");
        service.findTransactionForExternalIdAndGatewayAccountExternalId("not-existing", gatewayAccountFixture.getExternalId());
    }
    

    @Test
    public void paymentSubmittedToProvider_shouldUpdateTransactionAsPending_andRegisterAPaymentSubmittedEvent() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(NEW)
                .toEntity();
        service.paymentSubmittedToProviderFor(transaction, LocalDate.now());

        verify(mockedTransactionDao).updateState(transaction.getId(), PaymentState.PENDING);
        verify(mockedDirectDebitEventService).registerPaymentSubmittedToProviderEventFor(transaction);
        assertThat(transaction.getState(), is(PENDING));
    }

    @Test
    public void paymentAcknowledgedFor_shouldRegisterAPaymentPendingEvent() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING)
                .toEntity();

        service.paymentAcknowledgedFor(transaction);

        verify(mockedDirectDebitEventService).registerPaymentAcknowledgedEventFor(transaction);
        verifyZeroInteractions(mockedTransactionDao);
        assertThat(transaction.getState(), is(PENDING));
    }

    @Test
    public void paymentPaidOutFor_shouldSetPaymentAsSucceeded_andRegisterAPaidOutEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING)
                .toEntity();

        service.paymentPaidOutFor(transaction);

        verify(mockedTransactionDao).updateState(transaction.getId(), SUCCESS);
        verify(mockedDirectDebitEventService).registerPaymentPaidOutEventFor(transaction);
        assertThat(transaction.getState(), is(SUCCESS));
    }

    @Test
    public void paymentFailedFor_shouldSetPaymentAsFailed_andRegisterAPaymentFailedEvent_andDoNotSendEmail() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING)
                .toEntity();

        service.paymentFailedWithoutEmailFor(transaction);

        verify(mockedUserNotificationService, times(0)).sendPaymentFailedEmailFor(transaction);
        verify(mockedTransactionDao).updateState(transaction.getId(), FAILED);
        verify(mockedDirectDebitEventService).registerPaymentFailedEventFor(transaction);
        assertThat(transaction.getState(), is(FAILED));
    }

    @Test
    public void paymentFailedFor_shouldSetPaymentAsFailed_andRegisterAPaymentFailedEvent_andSendEmail() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING)
                .toEntity();

        service.paymentFailedWithEmailFor(transaction);

        verify(mockedUserNotificationService, times(1)).sendPaymentFailedEmailFor(transaction);
        verify(mockedTransactionDao).updateState(transaction.getId(), FAILED);
        verify(mockedDirectDebitEventService).registerPaymentFailedEventFor(transaction);
        assertThat(transaction.getState(), is(FAILED));
    }

    @Test
    public void payoutPaid_shouldRegisterAPayoutPaidEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(SUCCESS)
                .toEntity();

        service.payoutPaidFor(transaction);

        verify(mockedDirectDebitEventService).registerPayoutPaidEventFor(transaction);
        verifyZeroInteractions(mockedTransactionDao);
        assertThat(transaction.getState(), is(SUCCESS));
    }


    @Test
    public void findPaymentSubmittedToBankEventFor_shouldFindEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING)
                .toEntity();

        DirectDebitEvent directDebitEvent = DirectDebitEventFixture.aDirectDebitEventFixture().toEntity();

        when(mockedDirectDebitEventService.findBy(transaction.getId(), Type.CHARGE,
                PAYMENT_SUBMITTED_TO_BANK))
                .thenReturn(Optional.of(directDebitEvent));

        DirectDebitEvent foundDirectDebitEvent = service.findPaymentSubmittedEventFor(transaction).get();

        assertThat(foundDirectDebitEvent, is(directDebitEvent));
    }

    @Test
    public void paymentCancelledFor_shouldUpdateTransactionAsCancelled_shouldRegisterAPaymentCancelledEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(NEW)
                .toEntity();

        service.paymentCancelledFor(transaction);

        verify(mockedDirectDebitEventService).registerPaymentCancelledEventFor(mandateFixture.toEntity(), transaction);
        verify(mockedTransactionDao).updateState(transaction.getId(), CANCELLED);
        assertThat(transaction.getState(), is(CANCELLED));
    }
    
    @Test
    public void userNotEligibleFor_shouldUpdateTransactionAsCancelled_shouldRegisterAUserNotEligibledEvent_ifMandateIsOneOff() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(NEW)
                .toEntity();

        service.paymentMethodChangedFor(transaction);

        verify(mockedDirectDebitEventService).registerPaymentMethodChangedEventFor(mandateFixture.toEntity());
        verify(mockedTransactionDao).updateState(transaction.getId(), USER_CANCEL_NOT_ELIGIBLE);
        assertThat(transaction.getState(), is(USER_CANCEL_NOT_ELIGIBLE));
    }
    
    @Test
    public void paymentExpired_shouldSetStatusToExpired() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(NEW)
                .toEntity();
        service.paymentExpired(transaction);
        verify(mockedDirectDebitEventService).registerPaymentExpiredEventFor(transaction);
        verify(mockedTransactionDao).updateState(transaction.getId(), EXPIRED);
        assertThat(transaction.getState(), is(EXPIRED));
    }
}
