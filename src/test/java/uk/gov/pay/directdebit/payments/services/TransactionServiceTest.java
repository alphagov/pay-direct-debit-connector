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
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private TransactionDao mockedTransactionDao;

    private TransactionService service;
    @Mock
    private PaymentRequestEventService mockedPaymentRequestEventService;

    private PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture();
    @Before
    public void setUp() throws Exception {
        service = new TransactionService(mockedTransactionDao, mockedPaymentRequestEventService);
    }

    @Test
    public void shouldCreateATransactionAndEvent() {
        ArgumentCaptor<PaymentRequest> prCaptor = forClass(PaymentRequest.class);
        ArgumentCaptor<PaymentRequestEvent> preCaptor = forClass(PaymentRequestEvent.class);
        Transaction transaction = service.createChargeFor(paymentRequestFixture.toEntity());
        verify(mockedPaymentRequestEventService).insertEventFor(prCaptor.capture(), preCaptor.capture());

        PaymentRequestEvent createdPaymentRequestEvent = preCaptor.getValue();

        assertThat(transaction.getId(), is(notNullValue()));
        assertThat(transaction.getPaymentRequestId(), is(paymentRequestFixture.getId()));
        assertThat(transaction.getPaymentRequestExternalId(), is(paymentRequestFixture.getExternalId()));
        assertThat(transaction.getAmount(), is(paymentRequestFixture.getAmount()));
        assertThat(transaction.getType(), is(Transaction.Type.CHARGE));
        assertThat(transaction.getState(), is(PaymentState.NEW));
        assertThat(createdPaymentRequestEvent.getPaymentRequestId(), is(paymentRequestFixture.getId()));
        assertThat(createdPaymentRequestEvent.getEventType(), is(PaymentRequestEvent.Type.CHARGE));
        assertThat(createdPaymentRequestEvent.getEvent(), is(PaymentRequestEvent.SupportedEvent.CHARGE_CREATED));
        assertThat(createdPaymentRequestEvent.getEventDate(),
                is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void shouldFindATransactionByExternalId() {
        TransactionFixture transactionFixture = TransactionFixture
                .aTransactionFixture();
        when(mockedTransactionDao.findByPaymentRequestExternalId(paymentRequestFixture.getExternalId()))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        Transaction foundTransaction = service.findChargeForExternalId(paymentRequestFixture.getExternalId());
        assertThat(foundTransaction.getId(), is(notNullValue()));
        assertThat(foundTransaction.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(foundTransaction.getPaymentRequestExternalId(), is(transactionFixture.getPaymentRequestExternalId()));
        assertThat(foundTransaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(foundTransaction.getType(), is(transactionFixture.getType()));
        assertThat(foundTransaction.getState(), is(transactionFixture.getState()));
    }
    @Test
    public void shouldThrow_ifNoTransactionExistsWithExternalId()  {
        thrown.expect(ChargeNotFoundException.class);
        thrown.expectMessage("No charges found for payment request with id: not-existing");
        thrown.reportMissingExceptionWithMessage("ChargeNotFoundException expected");
        service.findChargeForExternalId("not-existing");
    }

    @Test
    public void shouldUpdateTransactionStateAndRegisterEventWhenAPayerIsCreated() throws Exception {
        TransactionFixture transactionFixture = TransactionFixture
                .aTransactionFixture()
                .withState(PaymentState.PROCESSING_DIRECT_DEBIT_DETAILS);
        Transaction newTransaction = service.payerCreatedFor(transactionFixture.toEntity());
        assertThat(newTransaction.getId(), is(notNullValue()));
        assertThat(newTransaction.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(newTransaction.getPaymentRequestExternalId(), is(transactionFixture.getPaymentRequestExternalId()));
        assertThat(newTransaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(newTransaction.getType(), is(transactionFixture.getType()));
        assertThat(newTransaction.getState(), is(PaymentState.AWAITING_CONFIRMATION));
        verify(mockedPaymentRequestEventService).registerPayerCreatedEventFor(newTransaction);
    }

    @Test
    public void shouldUpdateTransactionStateAndRegisterEventWhenReceivingDDDetails() throws Exception {
        TransactionFixture transactionFixture = TransactionFixture
                .aTransactionFixture()
                .withState(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS);
        when(mockedTransactionDao.findByPaymentRequestExternalId(transactionFixture.getPaymentRequestExternalId()))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        Transaction newTransaction = service.receiveDirectDebitDetailsFor(transactionFixture.getPaymentRequestExternalId());
        assertThat(newTransaction.getId(), is(notNullValue()));
        assertThat(newTransaction.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(newTransaction.getPaymentRequestExternalId(), is(transactionFixture.getPaymentRequestExternalId()));
        assertThat(newTransaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(newTransaction.getType(), is(transactionFixture.getType()));
        assertThat(newTransaction.getState(), is(PaymentState.PROCESSING_DIRECT_DEBIT_DETAILS));
        verify(mockedPaymentRequestEventService).registerDirectDebitReceivedEventFor(newTransaction);
    }

    @Test
    public void shouldUpdateTransactionStateAndRegisterEventWhenExchangingTokens() throws Exception {
        String token = "token";
        TransactionFixture transactionFixture = TransactionFixture
                .aTransactionFixture()
                .withState(PaymentState.NEW);
        when(mockedTransactionDao.findByTokenId(token))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        Transaction newTransaction = service.findChargeForToken(token).get();
        assertThat(newTransaction.getId(), is(notNullValue()));
        assertThat(newTransaction.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(newTransaction.getPaymentRequestExternalId(), is(transactionFixture.getPaymentRequestExternalId()));
        assertThat(newTransaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(newTransaction.getType(), is(transactionFixture.getType()));
        assertThat(newTransaction.getState(), is(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS));
        verify(mockedPaymentRequestEventService).registerTokenExchangedEventFor(newTransaction);
    }

    @Test
    public void shouldNotReturnATransactionIfNoTransactionExistsForToken() throws Exception {
        when(mockedTransactionDao.findByTokenId("not-existing"))
                .thenReturn(Optional.empty());
        assertThat(service.findChargeForToken("not-existing").isPresent(), is(false));
        verifyNoMoreInteractions(mockedPaymentRequestEventService);
    }
}
