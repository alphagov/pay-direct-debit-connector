package uk.gov.pay.directdebit.payments.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.events.services.DirectDebitEventService;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
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
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentIdAndChargeDate;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.Payment.PaymentBuilder.fromPayment;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.EXPIRED;
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
    
    @Mock
    private GovUkPayEventService mockedGovUkPayEventService;
    
    @InjectMocks
    private PaymentService service;

    private PayerFixture payerFixture = PayerFixture.aPayerFixture();
    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).withPayerFixture(payerFixture);
    private PaymentFixture paymentFixture = PaymentFixture.aPaymentFixture().withMandateFixture(mandateFixture);

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
    public void shouldCreateAPaymentResponseFromAValidTransaction() {
        var paymentResponse = PaymentResponse.from(paymentFixture.toEntity());
        assertThat(paymentResponse.getAmount(), is(paymentFixture.getAmount()));
        assertThat(paymentResponse.getPaymentExternalId(), is(paymentFixture.getExternalId()));
        assertThat(paymentResponse.getDescription(), is(paymentFixture.getDescription()));
        assertThat(paymentResponse.getReference(), is(paymentFixture.getReference()));
        assertThat(paymentResponse.getPaymentProvider(), is(gatewayAccountFixture.getPaymentProvider()));
    }
    @Test
    public void shouldCreateATransactionResponseWithLinksFromAValidTransaction() {
        var paymentResponse = PaymentResponse.from(paymentFixture.toEntity());

        assertThat(paymentResponse.getAmount(), is(paymentFixture.getAmount()));
        assertThat(paymentResponse.getDescription(), is(paymentFixture.getDescription()));
        assertThat(paymentResponse.getReference(), is(paymentFixture.getReference()));
    }

    @Test
    public void paymentSubmittedToProvider_shouldUpdatePaymentAsPending_andRegisterAPaymentSubmittedEvent() {
        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(NEW)
                .toEntity();

        service.paymentSubmittedToProviderFor(payment);

        Payment updatedPayment = fromPayment(payment).withState(PENDING).build();
        verify(mockedPaymentDao).updateState(updatedPayment.getId(), PaymentState.PENDING);
        verify(mockedDirectDebitEventService).registerPaymentSubmittedToProviderEventFor(updatedPayment);
        verify(mockedGovUkPayEventService).storeEventForPayment(payment, PAYMENT_SUBMITTED);
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

        verify(mockedDirectDebitEventService).registerPaymentPaidOutEventFor(payment);
    }

    @Test
    public void paymentFailedFor_shouldSetPaymentAsFailed_andRegisterAPaymentFailedEvent_andDoNotSendEmail() {

        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withState(PENDING)
                .toEntity();

        service.paymentFailedWithoutEmailFor(payment);

        verify(mockedUserNotificationService, times(0)).sendPaymentFailedEmailFor(payment);
        verify(mockedDirectDebitEventService).registerPaymentFailedEventFor(payment);
    }

    @Test
    public void paymentFailedFor_shouldSetPaymentAsFailed_andRegisterAPaymentFailedEvent_andSendEmail() {
        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withState(PENDING)
                .toEntity();

        service.paymentFailedWithEmailFor(payment);

        verify(mockedUserNotificationService).sendPaymentFailedEmailFor(payment);
        verify(mockedDirectDebitEventService).registerPaymentFailedEventFor(payment);
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
    public void createShouldCreatePaymentAndRegisterPaymentCreatedEvent() {
        Mandate mandate = mandateFixture.toEntity();

        Payment returnedPayment = service.createPayment(123456L,"a description", "a reference", mandate);

        assertThat(returnedPayment.getAmount(), is(123456L));
        assertThat(returnedPayment.getMandate(), is(mandate));
        assertThat(returnedPayment.getState(), is(NEW));
        assertThat(returnedPayment.getDescription(), is("a description"));
        assertThat(returnedPayment.getReference(), is("a reference"));
        assertThat(returnedPayment.getProviderId(), is(Optional.empty()));
        assertThat(returnedPayment.getChargeDate(), is(Optional.empty()));

        verify(mockedDirectDebitEventService).registerPaymentCreatedEventFor(returnedPayment);
    }

    @Test
    public void submitPaymentToProvider_shouldSubmitAndRegisterPaymentSubmittedEvent() {
        Mandate mandate = mandateFixture.toEntity();

        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withAmount(123456L)
                .withMandateFixture(mandateFixture)
                .withDescription("a description")
                .withReference("a reference")
                .withState(NEW)
                .toEntity();

        SandboxPaymentId sandboxPaymentId = SandboxPaymentId.valueOf("123");
        LocalDate chargeDate = LocalDate.now().plusDays(2);

        when(mockedPaymentProviderFactory.getCommandServiceFor(mandate.getGatewayAccount().getPaymentProvider())).thenReturn(mockedSandboxService);
        when(mockedSandboxService.collect(mandate, payment)).thenReturn(new PaymentProviderPaymentIdAndChargeDate(sandboxPaymentId, chargeDate));

        Payment returnedPayment = service.submitPaymentToProvider(payment);

        assertThat(returnedPayment.getAmount(), is(payment.getAmount()));
        assertThat(returnedPayment.getMandate(), is(payment.getMandate()));
        assertThat(returnedPayment.getState(), is(PENDING));
        assertThat(returnedPayment.getDescription(), is("a description"));
        assertThat(returnedPayment.getReference(), is("a reference"));
        assertThat(returnedPayment.getProviderId(), is(Optional.of(sandboxPaymentId)));
        assertThat(returnedPayment.getChargeDate(), is(Optional.of(chargeDate)));

        Payment expectedPaymentWithStatePending = fromPayment(returnedPayment).withState(PENDING).build();
        verify(mockedDirectDebitEventService).registerPaymentSubmittedToProviderEventFor(expectedPaymentWithStatePending);
    }

}
