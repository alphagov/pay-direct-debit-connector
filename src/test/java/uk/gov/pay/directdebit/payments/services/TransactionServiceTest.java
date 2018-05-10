package uk.gov.pay.directdebit.payments.services;

import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture.aPaymentRequestEventFixture;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUBMITTING_DIRECT_DEBIT_PAYMENT;
import static uk.gov.pay.directdebit.payments.model.PaymentState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.NEW;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PENDING_DIRECT_DEBIT_PAYMENT;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUCCESS;
import static uk.gov.pay.directdebit.payments.model.PaymentState.USER_CANCEL_NOT_ELIGIBLE;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private TransactionDao mockedTransactionDao;

    @Mock
    private UserNotificationService mockedUserNotificationService;

    private Payer payer = PayerFixture.aPayerFixture().toEntity();
    private TransactionService service;
    @Mock
    private PaymentRequestEventService mockedPaymentRequestEventService;

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture()
            .withGatewayAccountId(gatewayAccountFixture.getId());

    @Before
    public void setUp() {
        service = new TransactionService(mockedTransactionDao, mockedPaymentRequestEventService, mockedUserNotificationService);
    }

    @Test
    public void createChargeFor_shouldCreateATransactionAndAnEvent() {
        ArgumentCaptor<PaymentRequest> prCaptor = forClass(PaymentRequest.class);
        ArgumentCaptor<PaymentRequestEvent> preCaptor = forClass(PaymentRequestEvent.class);
        Transaction transaction = service.createChargeFor(paymentRequestFixture.toEntity(), gatewayAccountFixture.toEntity());
        verify(mockedPaymentRequestEventService).insertEventFor(prCaptor.capture(), preCaptor.capture());
        verify(mockedTransactionDao).insert(transaction);
        PaymentRequestEvent createdPaymentRequestEvent = preCaptor.getValue();
        PaymentRequest transactionPaymentRequest = transaction.getPaymentRequest();

        assertThat(transaction.getId(), is(notNullValue()));
        assertThat(transactionPaymentRequest.getId(), is(paymentRequestFixture.getId()));
        assertThat(transactionPaymentRequest.getExternalId(), is(paymentRequestFixture.getExternalId()));
        assertThat(transactionPaymentRequest.getReturnUrl(), is(paymentRequestFixture.getReturnUrl()));
        assertThat(transaction.getGatewayAccountId(), is(paymentRequestFixture.getGatewayAccountId()));
        assertThat(transactionPaymentRequest.getDescription(), is(paymentRequestFixture.getDescription()));
        assertThat(transactionPaymentRequest.getReference(), is(paymentRequestFixture.getReference()));
        assertThat(transaction.getAmount(), is(paymentRequestFixture.getAmount()));
        assertThat(transaction.getType(), is(Transaction.Type.CHARGE));
        assertThat(transaction.getState(), is(NEW));
        assertThat(createdPaymentRequestEvent.getPaymentRequestId(), is(paymentRequestFixture.getId()));
        assertThat(createdPaymentRequestEvent.getEventType(), is(Type.CHARGE));
        assertThat(createdPaymentRequestEvent.getEvent(), is(CHARGE_CREATED));
        assertThat(createdPaymentRequestEvent.getEventDate(),
                is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void findByPaymentRequestExternalIdAndAccountId_shouldFindATransaction() {
        TransactionFixture transactionFixture = TransactionFixture
                .aTransactionFixture();
        when(mockedTransactionDao.findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequestFixture.getExternalId(), gatewayAccountFixture.getExternalId()))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        Transaction foundTransaction = service.findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequestFixture.getExternalId(), gatewayAccountFixture.getExternalId());
        PaymentRequest paymentRequest = foundTransaction.getPaymentRequest();
        assertThat(foundTransaction.getId(), is(notNullValue()));
        assertThat(paymentRequest.getId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(paymentRequest.getExternalId(), is(transactionFixture.getPaymentRequestExternalId()));
        assertThat(paymentRequest.getReturnUrl(), is(transactionFixture.getPaymentRequestReturnUrl()));
        assertThat(foundTransaction.getGatewayAccountId(), is(transactionFixture.getGatewayAccountId()));
        assertThat(foundTransaction.getGatewayAccountExternalId(), is(transactionFixture.getGatewayAccountExternalId()));
        assertThat(paymentRequest.getDescription(), is(transactionFixture.getPaymentRequestDescription()));
        assertThat(paymentRequest.getReference(), is(transactionFixture.getPaymentRequestReference()));
        assertThat(foundTransaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(foundTransaction.getType(), is(transactionFixture.getType()));
        assertThat(foundTransaction.getState(), is(transactionFixture.getState()));
    }

    @Test
    public void findChargeForExternalIdAndGatewayAccountId_shouldThrow_ifNoTransactionExistsWithExternalId() {
        thrown.expect(ChargeNotFoundException.class);
        thrown.expectMessage("No charges found for payment request external id: not-existing");
        thrown.reportMissingExceptionWithMessage("ChargeNotFoundException expected");
        service.findTransactionForExternalIdAndGatewayAccountExternalId("not-existing", gatewayAccountFixture.getExternalId());
    }

    @Test
    public void payerCreatedFor_shouldRegisterAPayerCreatedEvent() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();

        service.payerCreatedFor(transaction);

        verify(mockedPaymentRequestEventService).registerPayerCreatedEventFor(transaction);
        verifyZeroInteractions(mockedTransactionDao);
        assertThat(transaction.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void payerEditedFor_shouldRegisterAPayerEditedEvent() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();

        service.payerEditedFor(transaction);

        verify(mockedPaymentRequestEventService).registerPayerEditedEventFor(transaction);
        verifyZeroInteractions(mockedTransactionDao);
        assertThat(transaction.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void findByPaymentRequestExternalIdAndAccountId_shouldRegisterEventWhenReceivingDirectDebitDetails() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();
        when(mockedTransactionDao.findTransactionForExternalIdAndGatewayAccountExternalId(transaction.getPaymentRequest().getExternalId(), gatewayAccountFixture.getExternalId()))
                .thenReturn(Optional.of(transaction));
         service.receiveDirectDebitDetailsFor(gatewayAccountFixture.getExternalId(), transaction.getPaymentRequest().getExternalId());
        verify(mockedPaymentRequestEventService).registerDirectDebitReceivedEventFor(transaction);
        assertThat(transaction.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void findTransactionForToken_shouldUpdateTransactionStateAndRegisterEventWhenExchangingTokens() {
        String token = "token";
        TransactionFixture transactionFixture = TransactionFixture
                .aTransactionFixture()
                .withState(NEW);
        when(mockedTransactionDao.findByTokenId(token))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        Transaction newTransaction = service.findTransactionForToken(token).get();
        PaymentRequest paymentRequest = newTransaction.getPaymentRequest();
        assertThat(newTransaction.getId(), is(notNullValue()));
        assertThat(paymentRequest.getId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(paymentRequest.getExternalId(), is(transactionFixture.getPaymentRequestExternalId()));
        assertThat(paymentRequest.getReturnUrl(), is(transactionFixture.getPaymentRequestReturnUrl()));
        assertThat(newTransaction.getGatewayAccountId(), is(transactionFixture.getGatewayAccountId()));
        assertThat(newTransaction.getGatewayAccountExternalId(), is(transactionFixture.getGatewayAccountExternalId()));
        assertThat(paymentRequest.getDescription(), is(transactionFixture.getPaymentRequestDescription()));
        assertThat(paymentRequest.getReference(), is(transactionFixture.getPaymentRequestReference()));
        assertThat(newTransaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(newTransaction.getType(), is(transactionFixture.getType()));
        assertThat(newTransaction.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
        verify(mockedPaymentRequestEventService).registerTokenExchangedEventFor(newTransaction);
    }

    @Test
    public void shouldUpdateTransactionStateAndRegisterEventWhenConfirmingDirectDebitDetails() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();
        when(mockedTransactionDao.findTransactionForExternalIdAndGatewayAccountExternalId(transaction.getPaymentRequest().getExternalId(), gatewayAccountFixture.getExternalId()))
                .thenReturn(Optional.of(transaction));
        Transaction newTransaction = service.confirmedDirectDebitDetailsFor(gatewayAccountFixture.getExternalId(), transaction.getPaymentRequest().getExternalId());
        PaymentRequest paymentRequest = newTransaction.getPaymentRequest();
        assertThat(newTransaction.getId(), is(notNullValue()));
        assertThat(paymentRequest.getId(), is(transaction.getPaymentRequest().getId()));
        assertThat(paymentRequest.getExternalId(), is(transaction.getPaymentRequest().getExternalId()));
        assertThat(paymentRequest.getReturnUrl(), is(transaction.getPaymentRequest().getReturnUrl()));
        assertThat(newTransaction.getGatewayAccountId(), is(transaction.getGatewayAccountId()));
        assertThat(newTransaction.getGatewayAccountExternalId(), is(transaction.getGatewayAccountExternalId()));
        assertThat(paymentRequest.getDescription(), is(transaction.getPaymentRequest().getDescription()));
        assertThat(paymentRequest.getReference(), is(transaction.getPaymentRequest().getReference()));
        assertThat(newTransaction.getAmount(), is(transaction.getAmount()));
        assertThat(newTransaction.getType(), is(transaction.getType()));
        assertThat(newTransaction.getState(), is(SUBMITTING_DIRECT_DEBIT_PAYMENT));
        verify(mockedPaymentRequestEventService).registerDirectDebitConfirmedEventFor(newTransaction);
    }
    

    @Test
    public void findTransactionForToken_shouldNotReturnATransactionIfNoTransactionExistsForToken() {
        when(mockedTransactionDao.findByTokenId("not-existing"))
                .thenReturn(Optional.empty());
        assertThat(service.findTransactionForToken("not-existing").isPresent(), is(false));
        verifyNoMoreInteractions(mockedPaymentRequestEventService);
    }

    @Test
    public void paymentSubmittedToProvider_shouldUpdateTransactionAsPending_andRegisterAPaymentSubmittedEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(SUBMITTING_DIRECT_DEBIT_PAYMENT)
                .toEntity();
        service.paymentSubmittedToProviderFor(transaction, payer, LocalDate.now());

        verify(mockedTransactionDao).updateState(transaction.getId(), PENDING_DIRECT_DEBIT_PAYMENT);
        verify(mockedPaymentRequestEventService).registerPaymentSubmittedToProviderEventFor(transaction);
        assertThat(transaction.getState(), is(PENDING_DIRECT_DEBIT_PAYMENT));
    }

    @Test
    public void paymentAcknowledgedFor_shouldRegisterAPaymentPendingEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        service.paymentAcknowledgedFor(transaction);

        verify(mockedPaymentRequestEventService).registerPaymentAcknowledgedEventFor(transaction);
        verifyZeroInteractions(mockedTransactionDao);
        assertThat(transaction.getState(), is(PENDING_DIRECT_DEBIT_PAYMENT));
    }

    @Test
    public void paymentCancelledFor_shouldUpdateTransactionAsCancelled_shouldRegisterAPaymentCancelledEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();

        service.paymentCancelledFor(transaction);

        verify(mockedPaymentRequestEventService).registerPaymentCancelledEventFor(transaction);
        verify(mockedTransactionDao).updateState(transaction.getId(), CANCELLED);
        assertThat(transaction.getState(), is(CANCELLED));
    }

    @Test
    public void paymentPaidOutFor_shouldSetPaymentAsSucceeded_andRegisterAPaidOutEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        service.paymentPaidOutFor(transaction);

        verify(mockedTransactionDao).updateState(transaction.getId(), SUCCESS);
        verify(mockedPaymentRequestEventService).registerPaymentPaidOutEventFor(transaction);
        assertThat(transaction.getState(), is(SUCCESS));
    }

    @Test
    public void paymentFailedFor_shouldSetPaymentAsFailed_andRegisterAPaymentFailedEvent_andDoNotSendEmail() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        service.paymentFailedWithoutEmailFor(transaction, payer);

        verify(mockedUserNotificationService, times(0)).sendPaymentFailedEmailFor(transaction, payer);
        verify(mockedTransactionDao).updateState(transaction.getId(), FAILED);
        verify(mockedPaymentRequestEventService).registerPaymentFailedEventFor(transaction);
        assertThat(transaction.getState(), is(FAILED));
    }

    @Test
    public void paymentFailedFor_shouldSetPaymentAsFailed_andRegisterAPaymentFailedEvent_andSendEmail() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        service.paymentFailedWithEmailFor(transaction, payer);

        verify(mockedUserNotificationService, times(1)).sendPaymentFailedEmailFor(transaction, payer);
        verify(mockedTransactionDao).updateState(transaction.getId(), FAILED);
        verify(mockedPaymentRequestEventService).registerPaymentFailedEventFor(transaction);
        assertThat(transaction.getState(), is(FAILED));
    }

    @Test
    public void payoutPaid_shouldRegisterAPayoutPaidEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(SUCCESS)
                .toEntity();

        service.payoutPaidFor(transaction);

        verify(mockedPaymentRequestEventService).registerPayoutPaidEventFor(transaction);
        verifyZeroInteractions(mockedTransactionDao);
        assertThat(transaction.getState(), is(SUCCESS));
    }


    @Test
    public void findPaymentSubmittedToBankEventFor_shouldFindEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        PaymentRequestEvent event = aPaymentRequestEventFixture().toEntity();

        when(mockedPaymentRequestEventService.findBy(transaction.getPaymentRequest().getId(), Type.CHARGE,
                PAYMENT_SUBMITTED_TO_BANK))
                .thenReturn(Optional.of(event));

        PaymentRequestEvent foundEvent = service.findPaymentSubmittedEventFor(transaction).get();

        assertThat(foundEvent, is(event));
    }

    @Test
    public void findMandatePendingEventFor_shouldFindEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        PaymentRequestEvent event = aPaymentRequestEventFixture().toEntity();

        when(mockedPaymentRequestEventService.findBy(transaction.getPaymentRequest().getId(), Type.MANDATE, MANDATE_PENDING))
                .thenReturn(Optional.of(event));

        PaymentRequestEvent foundEvent = service.findMandatePendingEventFor(transaction).get();

        assertThat(foundEvent, is(event));
    }

    @Test
    public void userNotEligibleFor_shouldUpdateTransactionAsCancelled_shouldRegisterAUserNotEligibledEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();

        service.paymentMethodChangedFor(transaction);

        verify(mockedPaymentRequestEventService).registerPaymentMethodChangedEventFor(transaction);
        verify(mockedTransactionDao).updateState(transaction.getId(), USER_CANCEL_NOT_ELIGIBLE);
        assertThat(transaction.getState(), is(USER_CANCEL_NOT_ELIGIBLE));
    }
}
