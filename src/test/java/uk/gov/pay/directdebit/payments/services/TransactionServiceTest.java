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
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture.aPaymentRequestEventFixture;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type;
import static uk.gov.pay.directdebit.payments.model.PaymentState.AWAITING_CONFIRMATION;
import static uk.gov.pay.directdebit.payments.model.PaymentState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.NEW;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PENDING_DIRECT_DEBIT_PAYMENT;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PROCESSING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PROCESSING_DIRECT_DEBIT_PAYMENT;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUCCESS;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private TransactionDao mockedTransactionDao;

    @Mock
    private MandateDao mockedMandateDao;

    @Mock
    private UserNotificationService mockedUserNotificationService;

    private Payer payer = PayerFixture.aPayerFixture().toEntity();
    private Mandate mandate = MandateFixture.aMandateFixture()
            .withPayerId(payer.getId())
            .toEntity();
    private TransactionService service;
    @Mock
    private PaymentRequestEventService mockedPaymentRequestEventService;

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture()
            .withGatewayAccountId(gatewayAccountFixture.getId());

    @Before
    public void setUp() {
        service = new TransactionService(mockedTransactionDao, mockedMandateDao, mockedPaymentRequestEventService, mockedUserNotificationService);
    }

    @Test
    public void createChargeFor_shouldCreateATransactionAndAnEvent() {
        ArgumentCaptor<PaymentRequest> prCaptor = forClass(PaymentRequest.class);
        ArgumentCaptor<PaymentRequestEvent> preCaptor = forClass(PaymentRequestEvent.class);
        Transaction transaction = service.createChargeFor(paymentRequestFixture.toEntity(), gatewayAccountFixture.toEntity());
        verify(mockedPaymentRequestEventService).insertEventFor(prCaptor.capture(), preCaptor.capture());
        verify(mockedTransactionDao).insert(transaction);
        PaymentRequestEvent createdPaymentRequestEvent = preCaptor.getValue();

        assertThat(transaction.getId(), is(notNullValue()));
        assertThat(transaction.getPaymentRequestId(), is(paymentRequestFixture.getId()));
        assertThat(transaction.getPaymentRequestExternalId(), is(paymentRequestFixture.getExternalId()));
        assertThat(transaction.getPaymentRequestReturnUrl(), is(paymentRequestFixture.getReturnUrl()));
        assertThat(transaction.getGatewayAccountId(), is(paymentRequestFixture.getGatewayAccountId()));
        assertThat(transaction.getPaymentRequestDescription(), is(paymentRequestFixture.getDescription()));
        assertThat(transaction.getPaymentRequestReference(), is(paymentRequestFixture.getReference()));
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
        when(mockedTransactionDao.findByPaymentRequestExternalIdAndAccountId(paymentRequestFixture.getExternalId(), gatewayAccountFixture.getId()))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        Transaction foundTransaction = service.findChargeForExternalIdAndGatewayAccountId(paymentRequestFixture.getExternalId(), gatewayAccountFixture.getId());
        assertThat(foundTransaction.getId(), is(notNullValue()));
        assertThat(foundTransaction.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(foundTransaction.getPaymentRequestExternalId(), is(transactionFixture.getPaymentRequestExternalId()));
        assertThat(foundTransaction.getPaymentRequestReturnUrl(), is(transactionFixture.getPaymentRequestReturnUrl()));
        assertThat(foundTransaction.getGatewayAccountId(), is(transactionFixture.getGatewayAccountId()));
        assertThat(foundTransaction.getGatewayAccountExternalId(), is(transactionFixture.getGatewayAccountExternalId()));
        assertThat(foundTransaction.getPaymentRequestDescription(), is(transactionFixture.getPaymentRequestDescription()));
        assertThat(foundTransaction.getPaymentRequestReference(), is(transactionFixture.getPaymentRequestReference()));
        assertThat(foundTransaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(foundTransaction.getType(), is(transactionFixture.getType()));
        assertThat(foundTransaction.getState(), is(transactionFixture.getState()));
    }

    @Test
    public void findChargeForExternalIdAndGatewayAccountId_shouldThrow_ifNoTransactionExistsWithExternalId() {
        thrown.expect(ChargeNotFoundException.class);
        thrown.expectMessage("No charges found for payment request external id: not-existing");
        thrown.reportMissingExceptionWithMessage("ChargeNotFoundException expected");
        service.findChargeForExternalIdAndGatewayAccountId("not-existing", gatewayAccountFixture.getId());
    }

    @Test
    public void payerCreatedFor_shouldUpdateTransactionStateAndRegisterEvent() throws Exception {
        TransactionFixture transactionFixture = TransactionFixture
                .aTransactionFixture()
                .withState(PROCESSING_DIRECT_DEBIT_DETAILS);
        Transaction newTransaction = service.payerCreatedFor(transactionFixture.toEntity());
        assertThat(newTransaction.getId(), is(notNullValue()));
        assertThat(newTransaction.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(newTransaction.getPaymentRequestExternalId(), is(transactionFixture.getPaymentRequestExternalId()));
        assertThat(newTransaction.getPaymentRequestReturnUrl(), is(transactionFixture.getPaymentRequestReturnUrl()));
        assertThat(newTransaction.getGatewayAccountId(), is(transactionFixture.getGatewayAccountId()));
        assertThat(newTransaction.getGatewayAccountExternalId(), is(transactionFixture.getGatewayAccountExternalId()));
        assertThat(newTransaction.getPaymentRequestDescription(), is(transactionFixture.getPaymentRequestDescription()));
        assertThat(newTransaction.getPaymentRequestReference(), is(transactionFixture.getPaymentRequestReference()));
        assertThat(newTransaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(newTransaction.getType(), is(transactionFixture.getType()));
        assertThat(newTransaction.getState(), is(AWAITING_CONFIRMATION));
        verify(mockedPaymentRequestEventService).registerPayerCreatedEventFor(newTransaction);
    }

    @Test
    public void findByPaymentRequestExternalIdAndAccountId_shouldUpdateTransactionStateAndRegisterEventWhenReceivingDDDetails() throws Exception {
        TransactionFixture transactionFixture = TransactionFixture
                .aTransactionFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS);
        when(mockedTransactionDao.findByPaymentRequestExternalIdAndAccountId(transactionFixture.getPaymentRequestExternalId(), gatewayAccountFixture.getId()))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        Transaction newTransaction = service.receiveDirectDebitDetailsFor(gatewayAccountFixture.getId(), transactionFixture.getPaymentRequestExternalId());
        assertThat(newTransaction.getId(), is(notNullValue()));
        assertThat(newTransaction.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(newTransaction.getPaymentRequestExternalId(), is(transactionFixture.getPaymentRequestExternalId()));
        assertThat(newTransaction.getPaymentRequestReturnUrl(), is(transactionFixture.getPaymentRequestReturnUrl()));
        assertThat(newTransaction.getGatewayAccountId(), is(transactionFixture.getGatewayAccountId()));
        assertThat(newTransaction.getGatewayAccountExternalId(), is(transactionFixture.getGatewayAccountExternalId()));
        assertThat(newTransaction.getPaymentRequestDescription(), is(transactionFixture.getPaymentRequestDescription()));
        assertThat(newTransaction.getPaymentRequestReference(), is(transactionFixture.getPaymentRequestReference()));
        assertThat(newTransaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(newTransaction.getType(), is(transactionFixture.getType()));
        assertThat(newTransaction.getState(), is(PROCESSING_DIRECT_DEBIT_DETAILS));
        verify(mockedPaymentRequestEventService).registerDirectDebitReceivedEventFor(newTransaction);
    }

    @Test
    public void shouldUpdateTransactionStateRegisterEventAndSendEmail_whenMandateFails() throws Exception {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT).toEntity();
        service.mandateFailedFor(transaction, payer);
        verify(mockedPaymentRequestEventService).registerMandateFailedEventFor(transaction);
        verify(mockedUserNotificationService).sendMandateFailedEmailFor(transaction, payer);
    }

    @Test
    public void shouldUpdateTransactionStateRegisterEventAndSendEmail_whenMandateIsCancelled() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT).toEntity();
        when(mockedMandateDao.findByTransactionId(transaction.getId())).thenReturn(Optional.of(mandate));
        service.mandateCancelledFor(transaction, payer);
        verify(mockedPaymentRequestEventService).registerMandateCancelledEventFor(transaction);
        verify(mockedUserNotificationService).sendMandateCancelledEmailFor(transaction, mandate, payer);
    }

    @Test
    public void shouldThrow_whenCantFindMandateForTransaction() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT).toEntity();
        when(mockedMandateDao.findByTransactionId(transaction.getId())).thenReturn(Optional.empty());
        thrown.expect(MandateNotFoundException.class);
        thrown.expectMessage("Couldn't find mandate for transaction with id: " + transaction.getId());
        thrown.reportMissingExceptionWithMessage("MandateNotFoundException expected");
        service.mandateCancelledFor(transaction, payer);
    }

    @Test
    public void findTransactionForToken_shouldUpdateTransactionStateAndRegisterEventWhenExchangingTokens() throws Exception {
        String token = "token";
        TransactionFixture transactionFixture = TransactionFixture
                .aTransactionFixture()
                .withState(NEW);
        when(mockedTransactionDao.findByTokenId(token))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        Transaction newTransaction = service.findTransactionForToken(token).get();
        assertThat(newTransaction.getId(), is(notNullValue()));
        assertThat(newTransaction.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(newTransaction.getPaymentRequestExternalId(), is(transactionFixture.getPaymentRequestExternalId()));
        assertThat(newTransaction.getPaymentRequestReturnUrl(), is(transactionFixture.getPaymentRequestReturnUrl()));
        assertThat(newTransaction.getGatewayAccountId(), is(transactionFixture.getGatewayAccountId()));
        assertThat(newTransaction.getGatewayAccountExternalId(), is(transactionFixture.getGatewayAccountExternalId()));
        assertThat(newTransaction.getPaymentRequestDescription(), is(transactionFixture.getPaymentRequestDescription()));
        assertThat(newTransaction.getPaymentRequestReference(), is(transactionFixture.getPaymentRequestReference()));
        assertThat(newTransaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(newTransaction.getType(), is(transactionFixture.getType()));
        assertThat(newTransaction.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
        verify(mockedPaymentRequestEventService).registerTokenExchangedEventFor(newTransaction);
    }

    @Test
    public void findTransactionForToken_shouldNotReturnATransactionIfNoTransactionExistsForToken() throws Exception {
        when(mockedTransactionDao.findByTokenId("not-existing"))
                .thenReturn(Optional.empty());
        assertThat(service.findTransactionForToken("not-existing").isPresent(), is(false));
        verifyNoMoreInteractions(mockedPaymentRequestEventService);
    }

    @Test
    public void paymentCreatedFor_shouldUpdateTransactionAsPending_andRegisterAPaymentCreatedEvent() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PROCESSING_DIRECT_DEBIT_PAYMENT)
                .toEntity();
        service.paymentCreatedFor(transaction, payer, LocalDate.now());

        verify(mockedTransactionDao).updateState(transaction.getId(), PENDING_DIRECT_DEBIT_PAYMENT);
        verify(mockedPaymentRequestEventService).registerPaymentCreatedEventFor(transaction);
        assertThat(transaction.getState(), is(PENDING_DIRECT_DEBIT_PAYMENT));
    }

    @Test
    public void paymentPendingFor_shouldRegisterAPaymentPendingEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        service.paymentPendingFor(transaction);

        verify(mockedPaymentRequestEventService).registerPaymentPendingEventFor(transaction);
        verifyZeroInteractions(mockedTransactionDao);
        assertThat(transaction.getState(), is(PENDING_DIRECT_DEBIT_PAYMENT));
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
    public void paymentFailedFor_shouldSetPaymentAsFailed_andRegisterAPaymentFailedEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        service.paymentFailedFor(transaction);

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
    public void mandatePendingFor_shouldRegisterAMandatePendingEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        service.mandatePendingFor(transaction);

        verify(mockedPaymentRequestEventService).registerMandatePendingEventFor(transaction);
        verifyZeroInteractions(mockedTransactionDao);
        assertThat(transaction.getState(), is(PENDING_DIRECT_DEBIT_PAYMENT));
    }

    @Test
    public void mandateActiveFor_shouldRegisterAMandateActiveEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        service.mandateActiveFor(transaction);

        verify(mockedPaymentRequestEventService).registerMandateActiveEventFor(transaction);
        verifyZeroInteractions(mockedTransactionDao);
        assertThat(transaction.getState(), is(PENDING_DIRECT_DEBIT_PAYMENT));
    }

    @Test
    public void findPaymentSubmittedEventFor_shouldFindEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        PaymentRequestEvent event = aPaymentRequestEventFixture().toEntity();

        when(mockedPaymentRequestEventService.findBy(transaction.getPaymentRequestId(), Type.CHARGE, PAYMENT_SUBMITTED))
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

        when(mockedPaymentRequestEventService.findBy(transaction.getPaymentRequestId(), Type.MANDATE, MANDATE_PENDING))
                .thenReturn(Optional.of(event));

        PaymentRequestEvent foundEvent = service.findMandatePendingEventFor(transaction).get();

        assertThat(foundEvent, is(event));
    }
}
