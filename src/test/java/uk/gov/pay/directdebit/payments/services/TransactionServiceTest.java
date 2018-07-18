package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.api.CollectPaymentResponse;
import uk.gov.pay.directdebit.payments.api.TransactionResponse;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.EXPIRED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.NEW;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PENDING;
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
    @Mock
    private TokenService mockedTokenService;
    @Mock
    private GatewayAccountDao mockedGatewayAccountDao;
    @Mock
    private DirectDebitConfig mockedDirectDebitConfig;
    @Mock
    private DirectDebitEventService mockedDirectDebitEventService;
    @Mock
    private LinksConfig mockedLinksConfig;
    @Mock
    private UriInfo mockedUriInfo;
    @Mock
    private UriBuilder mockedUriBuilder;
    
    private PayerFixture payerFixture = PayerFixture.aPayerFixture();
    private TransactionService service;


    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).withPayerFixture(payerFixture);
    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture);
    @Before
    public void setUp() throws URISyntaxException {
        service = new TransactionService(mockedTokenService, mockedGatewayAccountDao, mockedDirectDebitConfig, mockedTransactionDao,
                mockedDirectDebitEventService, mockedUserNotificationService, paymentProviderFactory);
        when(mockedDirectDebitConfig.getLinks()).thenReturn(mockedLinksConfig);
        when(mockedLinksConfig.getFrontendUrl()).thenReturn("https://frontend.test");
        when(mockedUriInfo.getBaseUriBuilder()).thenReturn(mockedUriBuilder);
        when(mockedUriBuilder.path(anyString())).thenReturn(mockedUriBuilder);
        when(mockedUriBuilder.build(any())).thenReturn(new URI("aaa"));
    }

    @Test
    public void findByTransactionExternalIdAndAccountId_shouldFindATransaction() {
        when(mockedTransactionDao.findByExternalId(transactionFixture.getExternalId()))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        Transaction foundTransaction = service.findTransactionForExternalId(transactionFixture.getExternalId());
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
        service.findTransactionForExternalId("not-existing");
    }
    
    @Test
    public void shouldCreateAndStoreATransactionFromAValidCreateTransactionRequest() {
        Map<String, String> createTransactionRequest = ImmutableMap.of(
                "amount", "2333",
                "description", "a description",
                "reference", "a reference"
        );
        CollectPaymentRequest collectPaymentRequest = CollectPaymentRequest.of(createTransactionRequest);
        
        when(mockedGatewayAccountDao.findByExternalId(gatewayAccountFixture.getExternalId()))
                .thenReturn(Optional.of((gatewayAccountFixture.toEntity())));
        Transaction transaction = service.createTransaction(
                collectPaymentRequest, 
                mandateFixture.toEntity(), 
                gatewayAccountFixture.getExternalId());
        
        assertThat(transaction.getId(), is(notNullValue()));
        assertThat(transaction.getExternalId(), is(notNullValue()));
        assertThat(transaction.getMandate(), is(mandateFixture.toEntity()));
        assertThat(transaction.getState(), is(NEW));
        assertThat(transaction.getAmount(), is(collectPaymentRequest.getAmount()));
        assertThat(transaction.getDescription(), is(collectPaymentRequest.getDescription()));
        assertThat(transaction.getReference(), is(collectPaymentRequest.getReference()));
        assertThat(transaction.getCreatedDate(), ZonedDateTimeMatchers
                .within(10, ChronoUnit.SECONDS, ZonedDateTime.now()));
        
        verify(mockedTransactionDao).insert(transaction);
        verify(mockedDirectDebitEventService).registerTransactionCreatedEventFor(transaction);
    }

    @Test
    public void shouldCreateACollectPaymentResponseFromAValidTransaction() {
        CollectPaymentResponse collectPaymentResponse = service
                .collectPaymentResponseWithSelfLink(transactionFixture.toEntity(),
                        gatewayAccountFixture.getExternalId(), mockedUriInfo);

        assertThat(collectPaymentResponse.getAmount(), is(transactionFixture.getAmount()));
        assertThat(collectPaymentResponse.getTransactionExternalId(), is(transactionFixture.getExternalId()));
        assertThat(collectPaymentResponse.getDescription(), is(transactionFixture.getDescription()));
        assertThat(collectPaymentResponse.getReference(), is(transactionFixture.getReference()));
        assertThat(collectPaymentResponse.getPaymentProvider(), is(gatewayAccountFixture.getPaymentProvider().toString()));
    }
    @Test
    public void shouldCreateATransactionResponseWithLinksFromAValidTransaction() {
        when(mockedTokenService.generateNewTokenFor(mandateFixture.toEntity())).thenReturn(new Token("token", mandateFixture.getId()));
        TransactionResponse transactionResponse = service
                .createPaymentResponseWithAllLinks(transactionFixture.toEntity(),
                        gatewayAccountFixture.getExternalId(), mockedUriInfo);

        assertThat(transactionResponse.getAmount(), is(transactionFixture.getAmount()));
        assertThat(transactionResponse.getDescription(), is(transactionFixture.getDescription()));
        assertThat(transactionResponse.getReference(), is(transactionFixture.getReference()));
        assertThat(transactionResponse.getReturnUrl(), is(transactionFixture.getMandateFixture().getReturnUrl()));
    }

    @Test
    public void oneOffPaymentSubmittedToProvider_shouldUpdateTransactionAsPending_andRegisterAPaymentSubmittedEvent() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(NEW)
                .toEntity();
        service.oneOffPaymentSubmittedToProviderFor(transaction, LocalDate.now());

        verify(mockedTransactionDao).updateState(transaction.getId(), PaymentState.PENDING);
        verify(mockedDirectDebitEventService).registerPaymentSubmittedToProviderEventFor(transaction);
        assertThat(transaction.getState(), is(PENDING));
    }

    @Test
    public void onDemandPaymentSubmittedToProvider_shouldUpdateTransactionAsPending_andRegisterAPaymentSubmittedEvent() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withMandateFixture(mandateFixture)
                .withState(NEW)
                .toEntity();
        service.onDemandPaymentSubmittedToProviderFor(transaction, LocalDate.now());

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
