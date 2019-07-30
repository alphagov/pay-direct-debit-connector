package uk.gov.pay.directdebit.payments.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentIdAndChargeDate;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.payments.model.Payment.PaymentBuilder.fromPayment;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUBMITTED_TO_PROVIDER;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {

    private static final SandboxMandateId SANDBOX_MANDATE_ID = SandboxMandateId.valueOf("sandbox-mandate-id");
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private PaymentDao mockedPaymentDao;

    @Mock
    private UserNotificationService mockedUserNotificationService;

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
    public void createShouldCreatePayment() {
        Mandate mandate = mandateFixture.toEntity();

        when(mockedGovUkPayEventService.storeEventAndUpdateStateForPayment(any(Payment.class), eq(PAYMENT_CREATED)))
                .thenAnswer(i -> i.getArguments()[0]);
        
        Payment returnedPayment = service.createPayment(123456L, "a description", "a reference", mandate);

        verify(mockedPaymentDao).insert(any(Payment.class));

        assertThat(returnedPayment.getAmount(), is(123456L));
        assertThat(returnedPayment.getMandate(), is(mandate));
        assertThat(returnedPayment.getState(), is(CREATED));
        assertThat(returnedPayment.getDescription(), is("a description"));
        assertThat(returnedPayment.getReference(), is("a reference"));
        assertThat(returnedPayment.getProviderId(), is(Optional.empty()));
        assertThat(returnedPayment.getChargeDate(), is(Optional.empty()));
        
        verify(mockedGovUkPayEventService).storeEventAndUpdateStateForPayment(returnedPayment, PAYMENT_CREATED);
    }

    @Test
    public void submitPaymentToProvider_shouldSubmitAndRegisterPaymentSubmittedEvent() {
        Mandate mandate = mandateFixture.withPaymentProviderId(SANDBOX_MANDATE_ID).toEntity();

        Payment payment = PaymentFixture
                .aPaymentFixture()
                .withAmount(123456L)
                .withMandateFixture(mandateFixture)
                .withDescription("a description")
                .withReference("a reference")
                .withState(CREATED)
                .toEntity();

        SandboxPaymentId sandboxPaymentId = SandboxPaymentId.valueOf("123");
        LocalDate chargeDate = LocalDate.now().plusDays(2);

        Payment paymentWithProviderIdAndChargeDate = fromPayment(payment)
                .withProviderId(sandboxPaymentId)
                .withChargeDate(chargeDate)
                .build();

        when(mockedPaymentProviderFactory.getCommandServiceFor(mandate.getGatewayAccount().getPaymentProvider())).thenReturn(mockedSandboxService);
        when(mockedSandboxService.collect(payment, SANDBOX_MANDATE_ID)).thenReturn(new PaymentProviderPaymentIdAndChargeDate(sandboxPaymentId, chargeDate));

        when(mockedGovUkPayEventService.storeEventAndUpdateStateForPayment(paymentWithProviderIdAndChargeDate, PAYMENT_SUBMITTED))
                .thenAnswer(invocationOnMock -> {
                    Payment paymentToUpdate = invocationOnMock.getArgument(0, Payment.class);
                    return Payment.PaymentBuilder.fromPayment(paymentToUpdate).withState(SUBMITTED_TO_PROVIDER).build();
                });

        Payment returnedPayment = service.submitPaymentToProvider(payment, SANDBOX_MANDATE_ID);

        verify(mockedPaymentDao).updateProviderIdAndChargeDate(paymentWithProviderIdAndChargeDate);
        verify(mockedUserNotificationService).sendPaymentConfirmedEmailFor(paymentWithProviderIdAndChargeDate);

        Payment paymentWithUpdatedState = fromPayment(paymentWithProviderIdAndChargeDate)
                .withState(SUBMITTED_TO_PROVIDER)
                .build();

        assertThat(returnedPayment, is(paymentWithUpdatedState));
    }

}
