package uk.gov.pay.directdebit.notifications.services;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.ExecutorServiceConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.services.GatewayAccountService;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.config.NotifyClientFactory;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.UUID.randomUUID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class UserNotificationServiceTest {
    @Mock
    private NotificationClient mockNotifyClient;
    @Mock
    private NotifyClientFactory mockNotifyClientFactory;
    @Mock
    private DirectDebitConfig mockDirectDebitConfig;
    @Mock
    private LinksConfig mockLinksConfig;
    @Mock
    private SendEmailResponse mockNotificationCreatedResponse;
    @Mock
    private ExecutorServiceConfig mockExecutorConfiguration;
    @Mock
    private GatewayAccountService mockGatewayAccountService;
    @Mock
    private MetricRegistry mockMetricRegistry;
    @Mock
    private Histogram mockHistogram;
    @Mock
    private Counter mockCounter;
    private UserNotificationService userNotificationService;

    private static final String EMAIL = "ksdfhkjsdh@sdjkfh.test";
    private static final String MANDATE_FAILED_TEMPLATE = "mandate-failed-template";
    private static final String MANDATE_CANCELLED_TEMPLATE = "mandate-cancelled-template";
    private static final String PAYMENT_CONFIRMATION_TEMPLATE = "payment-confirmation-template";
    private Payer payer = aPayerFixture().withEmail(EMAIL).toEntity();
    private Mandate mandate = MandateFixture.aMandateFixture().toEntity();

    private Transaction transaction = aTransactionFixture()
            .withAmount(12345L)
            .toEntity();
    private GatewayAccount gatewayAccount = aGatewayAccountFixture().toEntity();

    @Before
    public void setUp() {
        when(mockDirectDebitConfig.getNotifyConfig()).thenReturn(mockNotifyClientFactory);
        when(mockDirectDebitConfig.getExecutorServiceConfig()).thenReturn(mockExecutorConfiguration);
        when(mockDirectDebitConfig.getLinks()).thenReturn(mockLinksConfig);
        when(mockNotifyClientFactory.getMandateFailedTemplateId()).thenReturn(MANDATE_FAILED_TEMPLATE);
        when(mockNotifyClientFactory.getMandateCancelledTemplateId()).thenReturn(MANDATE_CANCELLED_TEMPLATE);
        when(mockNotifyClientFactory.getPaymentConfirmedTemplateId()).thenReturn(PAYMENT_CONFIRMATION_TEMPLATE);
        when(mockExecutorConfiguration.getThreadsPerCpu()).thenReturn(2);
        when(mockGatewayAccountService.getGatewayAccountFor(transaction)).thenReturn(gatewayAccount);
        when(mockLinksConfig.getDirectDebitGuaranteeUrl()).thenReturn("https://frontend.url.test/direct-debit-guarantee");
        when(mockMetricRegistry.histogram(anyString())).thenReturn(mockHistogram);
        when(mockMetricRegistry.counter(anyString())).thenReturn(mockCounter);
        when(mockNotificationCreatedResponse.getNotificationId()).thenReturn(randomUUID());

    }

    @Test
    public void shouldSendMandateFailedEmailIfEmailNotifyIsEnabled() throws Exception {
        when(mockNotifyClientFactory.isEmailNotifyEnabled()).thenReturn(true);

        userNotificationService = new UserNotificationService(mockDirectDebitConfig, mockNotifyClient, mockMetricRegistry, mockGatewayAccountService);
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("mandate reference", mandate.getReference());
        emailPersonalisation.put("org name", gatewayAccount.getServiceName());
        emailPersonalisation.put("org phone", "+44 000-CAKE-000");
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        when(mockNotifyClient.sendEmail(MANDATE_FAILED_TEMPLATE, EMAIL, emailPersonalisation, null)).thenReturn(mockNotificationCreatedResponse);

        Future<Optional<String>> maybeNotificationId = userNotificationService.sendMandateFailedEmailFor(transaction, mandate, payer);
        maybeNotificationId.get(1000, TimeUnit.SECONDS);

        verify(mockNotifyClient).sendEmail(
                MANDATE_FAILED_TEMPLATE,
                EMAIL,
                emailPersonalisation,
                null
        );
    }

    @Test
    public void shouldSendMandateCancelledEmailIfEmailNotifyIsEnabled() throws Exception {
        when(mockNotifyClientFactory.isEmailNotifyEnabled()).thenReturn(true);
        userNotificationService = new UserNotificationService(mockDirectDebitConfig, mockNotifyClient, mockMetricRegistry, mockGatewayAccountService);

        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("mandate reference", mandate.getReference());
        emailPersonalisation.put("org name", gatewayAccount.getServiceName());
        emailPersonalisation.put("org phone", "+44 000-CAKE-000");
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        when(mockNotifyClient.sendEmail(MANDATE_CANCELLED_TEMPLATE, EMAIL, emailPersonalisation, null)).thenReturn(mockNotificationCreatedResponse);

        Future<Optional<String>> maybeNotificationId = userNotificationService.sendMandateCancelledEmailFor(transaction, mandate, payer);
        maybeNotificationId.get(1000, TimeUnit.SECONDS);

        verify(mockNotifyClient).sendEmail(
                MANDATE_CANCELLED_TEMPLATE,
                EMAIL,
                emailPersonalisation,
                null
        );
    }

    @Test
    public void shouldSendPaymentConfirmedEmailIfEmailNotifyIsEnabled() throws Exception {
        when(mockNotifyClientFactory.isEmailNotifyEnabled()).thenReturn(true);

        userNotificationService = new UserNotificationService(mockDirectDebitConfig, mockNotifyClient, mockMetricRegistry, mockGatewayAccountService);

        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("service name", gatewayAccount.getServiceName());
        emailPersonalisation.put("amount", "123.45");
        emailPersonalisation.put("payment reference", transaction.getPaymentRequestReference());
        emailPersonalisation.put("collection date", "21/05/2018");
        emailPersonalisation.put("bank account last 2 digits", "******" + payer.getAccountNumberLastTwoDigits());
        emailPersonalisation.put("SUN", "THE-CAKE-IS-A-LIE");
        emailPersonalisation.put("merchant address", "123 Rainbow Road, EC125Y, London");
        emailPersonalisation.put("merchant phone number", "+44 000-CAKE-000");
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        when(mockNotifyClient.sendEmail(PAYMENT_CONFIRMATION_TEMPLATE, EMAIL, emailPersonalisation, null)).thenReturn(mockNotificationCreatedResponse);

        Future<Optional<String>> maybeNotificationId = userNotificationService.sendPaymentConfirmedEmailFor(transaction, payer, LocalDate.parse("2018-05-21"));
        maybeNotificationId.get(1000, TimeUnit.SECONDS);

        verify(mockNotifyClient).sendEmail(
                PAYMENT_CONFIRMATION_TEMPLATE,
                EMAIL,
                emailPersonalisation,
                null);
    }

    @Test
    public void testEmailSendWhenEmailsNotifyDisabled() throws Exception {
        when(mockNotifyClientFactory.isEmailNotifyEnabled()).thenReturn(false);
        userNotificationService = new UserNotificationService(mockDirectDebitConfig, mockNotifyClient, mockMetricRegistry, mockGatewayAccountService);

        Future<Optional<String>> maybeNotificationId = userNotificationService.sendMandateFailedEmailFor(transaction, mandate, payer);
        maybeNotificationId.get(1000, TimeUnit.SECONDS);

        verifyZeroInteractions(mockNotifyClient);
    }

    @Test
    public void shouldRecordNotifyResponseTimesWhenSendEmailSucceeds() throws Exception {
        when(mockNotifyClientFactory.isEmailNotifyEnabled()).thenReturn(true);
        when(mockNotifyClient.sendEmail(any(), any(), any(), any())).thenReturn(mockNotificationCreatedResponse);

        userNotificationService = new UserNotificationService(mockDirectDebitConfig, mockNotifyClient, mockMetricRegistry, mockGatewayAccountService);

        Future<Optional<String>> maybeNotificationId = userNotificationService.sendMandateFailedEmailFor(transaction, mandate, payer);
        maybeNotificationId.get(1000, TimeUnit.SECONDS);
        verify(mockMetricRegistry).histogram("notify-operations.response_time");
        verify(mockHistogram).update(anyLong());
        verifyNoMoreInteractions(mockCounter);
    }

    @Test
    public void shouldRecordNotifyResponseTimesAndFailureWhenSendEmailFails() throws Exception {
        when(mockNotifyClientFactory.isEmailNotifyEnabled()).thenReturn(true);
        when(mockNotifyClient.sendEmail(any(), any(), any(), any())).thenThrow(NotificationClientException.class);

        userNotificationService = new UserNotificationService(mockDirectDebitConfig, mockNotifyClient, mockMetricRegistry, mockGatewayAccountService);

        Future<Optional<String>> maybeNotificationId = userNotificationService.sendMandateFailedEmailFor(transaction, mandate, payer);
        maybeNotificationId.get(1000, TimeUnit.SECONDS);
        verify(mockMetricRegistry).histogram("notify-operations.response_time");
        verify(mockHistogram).update(anyLong());
        verify(mockCounter).inc();
    }
}
