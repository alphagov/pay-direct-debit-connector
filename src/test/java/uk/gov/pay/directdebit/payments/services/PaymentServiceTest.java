package uk.gov.pay.directdebit.payments.services;

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
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
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
import static uk.gov.pay.directdebit.payments.model.Payment.PaymentBuilder.fromPayment;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.EXPIRED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.NEW;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUCCESS;
import static uk.gov.pay.directdebit.payments.model.PaymentState.USER_CANCEL_NOT_ELIGIBLE;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private PaymentDao mockedPaymentDao;
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
    @Mock
    private PaymentProviderFactory mockedPaymentProviderFactory;
    @Mock
    private SandboxService mockedSandboxService;
    
    private PayerFixture payerFixture = PayerFixture.aPayerFixture();
    private PaymentService service;


    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).withPayerFixture(payerFixture);
    private PaymentFixture paymentFixture = PaymentFixture.aPaymentFixture().withMandateFixture(mandateFixture);
    @Before
    public void setUp() throws URISyntaxException {
        service = new PaymentService(mockedTokenService, mockedGatewayAccountDao, mockedDirectDebitConfig, mockedPaymentDao,
                mockedDirectDebitEventService, mockedUserNotificationService, mockedPaymentProviderFactory);
        when(mockedDirectDebitConfig.getLinks()).thenReturn(mockedLinksConfig);
        when(mockedLinksConfig.getFrontendUrl()).thenReturn("https://frontend.test");
        when(mockedUriInfo.getBaseUriBuilder()).thenReturn(mockedUriBuilder);
        when(mockedUriBuilder.path(anyString())).thenReturn(mockedUriBuilder);
        when(mockedUriBuilder.build(any())).thenReturn(new URI("aaa"));
    }

    @Test
    public void findByTransactionExternalIdAndAccountId_shouldFindATransaction() {
        when(mockedPaymentDao.findByExternalId(paymentFixture.getExternalId()))
                .thenReturn(Optional.of(paymentFixture.toEntity()));
        Payment foundPayment = service.findPaymentForExternalId(paymentFixture.getExternalId());
        assertThat(foundPayment.getId(), is(notNullValue()));
        assertThat(foundPayment.getExternalId(), is(paymentFixture.getExternalId()));
        assertThat(foundPayment.getMandate(), is(mandateFixture.toEntity()));
        assertThat(foundPayment.getState(), is(paymentFixture.getState()));
        assertThat(foundPayment.getAmount(), is(paymentFixture.getAmount()));
        assertThat(foundPayment.getDescription(), is(paymentFixture.getDescription()));
        assertThat(foundPayment.getReference(), is(paymentFixture.getReference()));
        assertThat(foundPayment.getCreatedDate(), is(paymentFixture.getCreatedDate()));
    }

    @Test
    public void findChargeForExternalIdAndGatewayAccountId_shouldThrow_ifNoTransactionExistsWithExternalId() {
        thrown.expect(ChargeNotFoundException.class);
        thrown.expectMessage("No charges found for payment id: not-existing");
        thrown.reportMissingExceptionWithMessage("ChargeNotFoundException expected");
        service.findPaymentForExternalId("not-existing");
    }

    @Test
    public void shouldCreateACollectPaymentResponseFromAValidTransaction() {
        CollectPaymentResponse collectPaymentResponse = service
                .collectPaymentResponseWithSelfLink(paymentFixture.toEntity(),
                        gatewayAccountFixture.getExternalId(), mockedUriInfo);

        assertThat(collectPaymentResponse.getAmount(), is(paymentFixture.getAmount()));
        assertThat(collectPaymentResponse.getPaymentExternalId(), is(paymentFixture.getExternalId()));
        assertThat(collectPaymentResponse.getDescription(), is(paymentFixture.getDescription()));
        assertThat(collectPaymentResponse.getReference(), is(paymentFixture.getReference()));
        assertThat(collectPaymentResponse.getPaymentProvider(), is(gatewayAccountFixture.getPaymentProvider().toString()));
    }
    @Test
    public void shouldCreateATransactionResponseWithLinksFromAValidTransaction() {
        when(mockedTokenService.generateNewTokenFor(mandateFixture.toEntity())).thenReturn(new Token("token", mandateFixture.getId()));
        PaymentResponse paymentResponse = service
                .createPaymentResponseWithAllLinks(paymentFixture.toEntity(),
                        gatewayAccountFixture.getExternalId(), mockedUriInfo);

        assertThat(paymentResponse.getAmount(), is(paymentFixture.getAmount()));
        assertThat(paymentResponse.getDescription(), is(paymentFixture.getDescription()));
        assertThat(paymentResponse.getReference(), is(paymentFixture.getReference()));
        assertThat(paymentResponse.getReturnUrl(), is(paymentFixture.getMandateFixture().getReturnUrl()));
    }

    @Test
    public void onDemandPaymentSubmittedToProvider_shouldUpdateTransactionAsPending_andRegisterAPaymentSubmittedEvent() {
        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(NEW)
                .toEntity();

        service.onDemandPaymentSubmittedToProviderFor(payment, LocalDate.now());

        Payment updatedPayment = fromPayment(payment).withState(PENDING).build();
        verify(mockedPaymentDao).updateState(updatedPayment.getId(), PaymentState.PENDING);
        verify(mockedDirectDebitEventService).registerPaymentSubmittedToProviderEventFor(updatedPayment);
    }

    @Test
    public void paymentAcknowledgedFor_shouldRegisterAPaymentPendingEvent() {
        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withState(PENDING)
                .toEntity();

        service.paymentAcknowledgedFor(payment);

        verify(mockedDirectDebitEventService).registerPaymentAcknowledgedEventFor(payment);
        verifyZeroInteractions(mockedPaymentDao);
        assertThat(payment.getState(), is(PENDING));
    }

    @Test
    public void paymentPaidOutFor_shouldSetPaymentAsSucceeded_andRegisterAPaidOutEvent() {

        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withState(PENDING)
                .toEntity();

        service.paymentPaidOutFor(payment);

        Payment updatedPayment = fromPayment(payment).withState(SUCCESS).build();
        verify(mockedPaymentDao).updateState(updatedPayment.getId(), SUCCESS);
        verify(mockedDirectDebitEventService).registerPaymentPaidOutEventFor(updatedPayment);
    }

    @Test
    public void paymentFailedFor_shouldSetPaymentAsFailed_andRegisterAPaymentFailedEvent_andDoNotSendEmail() {

        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withState(PENDING)
                .toEntity();

        service.paymentFailedWithoutEmailFor(payment);

        Payment updatedPayment = fromPayment(payment).withState(FAILED).build();
        verify(mockedUserNotificationService, times(0)).sendPaymentFailedEmailFor(updatedPayment);
        verify(mockedPaymentDao).updateState(payment.getId(), FAILED);
        verify(mockedDirectDebitEventService).registerPaymentFailedEventFor(updatedPayment);
    }

    @Test
    public void paymentFailedFor_shouldSetPaymentAsFailed_andRegisterAPaymentFailedEvent_andSendEmail() {
        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withState(PENDING)
                .toEntity();

        service.paymentFailedWithEmailFor(payment);

        verify(mockedUserNotificationService).sendPaymentFailedEmailFor(payment);

        Payment updatedPayment = fromPayment(payment).withState(FAILED).build();
        verify(mockedPaymentDao).updateState(updatedPayment.getId(), FAILED);
        verify(mockedDirectDebitEventService).registerPaymentFailedEventFor(updatedPayment);
    }

    @Test
    public void payoutPaid_shouldRegisterAPayoutPaidEvent() {

        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withState(SUCCESS)
                .toEntity();

        service.payoutPaidFor(payment);

        verify(mockedDirectDebitEventService).registerPayoutPaidEventFor(payment);
        verifyZeroInteractions(mockedPaymentDao);
        assertThat(payment.getState(), is(SUCCESS));
    }


    @Test
    public void findPaymentSubmittedToBankEventFor_shouldFindEvent() {

        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withState(PENDING)
                .toEntity();

        DirectDebitEvent directDebitEvent = DirectDebitEventFixture.aDirectDebitEventFixture().toEntity();

        when(mockedDirectDebitEventService.findBy(payment.getId(), Type.CHARGE,
                PAYMENT_SUBMITTED_TO_BANK))
                .thenReturn(Optional.of(directDebitEvent));

        DirectDebitEvent foundDirectDebitEvent = service.findPaymentSubmittedEventFor(payment).get();

        assertThat(foundDirectDebitEvent, is(directDebitEvent));
    }

    @Test
    public void paymentCancelledFor_shouldUpdateTransactionAsCancelled_shouldRegisterAPaymentCancelledEvent() {

        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(NEW)
                .toEntity();

        service.paymentCancelledFor(payment);

        Payment updatedPayment = fromPayment(payment).withState(CANCELLED).build();
        verify(mockedDirectDebitEventService).registerPaymentCancelledEventFor(mandateFixture.toEntity(), updatedPayment);
        verify(mockedPaymentDao).updateState(updatedPayment.getId(), CANCELLED);
    }
    
    @Test
    public void userNotEligibleFor_shouldUpdateTransactionAsCancelled_shouldRegisterAUserNotEligibledEvent_ifMandateIsOneOff() {
        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(NEW)
                .toEntity();

        service.paymentMethodChangedFor(payment);

        Payment updatedPayment = fromPayment(payment).withState(USER_CANCEL_NOT_ELIGIBLE).build();
        verify(mockedDirectDebitEventService).registerPaymentMethodChangedEventFor(mandateFixture.toEntity());
        verify(mockedPaymentDao).updateState(updatedPayment.getId(), USER_CANCEL_NOT_ELIGIBLE);
    }
    
    @Test
    public void paymentExpired_shouldSetStatusToExpired() {
        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(NEW)
                .toEntity();

        service.paymentExpired(payment);

        Payment updatedPayment = fromPayment(payment).withState(EXPIRED).build();
        verify(mockedDirectDebitEventService).registerPaymentExpiredEventFor(updatedPayment);
        verify(mockedPaymentDao).updateState(updatedPayment.getId(), EXPIRED);
    }

    @Test
    public void collect_shouldCreateAPaymentAndRegisterPaymentSubmittedEvent() {
        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withAmount(123456L)
                .withMandateFixture(mandateFixture)
                .withDescription("a description")
                .withReference("a reference")
                .withState(NEW)
                .toEntity();

        CollectPaymentRequest collectPaymentRequest = new CollectPaymentRequest(
                mandateFixture.getExternalId(),
                payment.getAmount(),
                payment.getDescription(),
                payment.getReference());

        when(mockedPaymentProviderFactory.getCommandServiceFor(mandateFixture.toEntity().getGatewayAccount().getPaymentProvider())).thenReturn(mockedSandboxService);
        when(mockedGatewayAccountDao.findByExternalId(gatewayAccountFixture.getExternalId())).thenReturn(Optional.of(gatewayAccountFixture.toEntity()));

        Payment returnedPayment = service.createAndCollectPayment(gatewayAccountFixture.toEntity(), mandateFixture.toEntity(), collectPaymentRequest);

        assertThat(returnedPayment.getAmount(), is(payment.getAmount()));
        assertThat(returnedPayment.getMandate(), is(payment.getMandate()));
        assertThat(returnedPayment.getState(), is(PENDING));
        assertThat(returnedPayment.getDescription(), is("a description"));
        assertThat(returnedPayment.getReference(), is("a reference"));

        Payment expectedPaymentWithStatePending = fromPayment(returnedPayment).withState(PENDING).build();
        verify(mockedDirectDebitEventService).registerPaymentSubmittedToProviderEventFor(expectedPaymentWithStatePending);
    }
}
