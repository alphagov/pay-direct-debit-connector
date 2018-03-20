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
import uk.gov.pay.directdebit.notifications.config.NotifyClientFactory;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.UUID.randomUUID;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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
    private Payer payer = PayerFixture.aPayerFixture().withEmail(EMAIL).toEntity();
    private Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();
    private GatewayAccount gatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().toEntity();

    @Before
    public void setUp() {
        when(mockDirectDebitConfig.getNotifyConfig()).thenReturn(mockNotifyClientFactory);
        when(mockDirectDebitConfig.getExecutorServiceConfig()).thenReturn(mockExecutorConfiguration);
        when(mockDirectDebitConfig.getLinks()).thenReturn(mockLinksConfig);
        when(mockNotifyClientFactory.getMandateFailedTemplateId()).thenReturn("some-template");
        when(mockExecutorConfiguration.getThreadsPerCpu()).thenReturn(2);
        when(mockGatewayAccountService.getGatewayAccountFor(transaction)).thenReturn(gatewayAccount);
        when(mockLinksConfig.getFrontendUrl()).thenReturn("https://frontend.url.test");
        when(mockMetricRegistry.histogram(anyString())).thenReturn(mockHistogram);
        when(mockMetricRegistry.counter(anyString())).thenReturn(mockCounter);
        when(mockNotificationCreatedResponse.getNotificationId()).thenReturn(randomUUID());

    }

    @Test
    public void shouldSendEmailIfEmailNotifyIsEnabled() throws Exception {
        when(mockNotifyClientFactory.isEmailNotifyEnabled()).thenReturn(true);
        when(mockNotifyClient.sendEmail(any(), any(), any(), any())).thenReturn(mockNotificationCreatedResponse);

        userNotificationService = new UserNotificationService(mockDirectDebitConfig, mockNotifyClient, mockMetricRegistry, mockGatewayAccountService);
        Future<Optional<String>> idF = userNotificationService.sendMandateFailedEmail(payer, transaction);
        idF.get(1000, TimeUnit.SECONDS);

        HashMap<String, String> map = new HashMap<>();
        map.put("org name", gatewayAccount.getServiceName());
        map.put("org phone", "000-CAKE-000");
        map.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");
        verify(mockNotifyClient).sendEmail(
                mockNotifyClientFactory.getMandateFailedTemplateId(),
                EMAIL,
                map,
                null
        );
    }

    @Test
    public void testEmailSendingThrowsExceptionForMissingTemplate() throws Exception {
        try {
            reset(mockNotifyClientFactory);
            when(mockNotifyClientFactory.isEmailNotifyEnabled()).thenReturn(true);
            userNotificationService = new UserNotificationService(mockDirectDebitConfig, mockNotifyClient, mockMetricRegistry, mockGatewayAccountService);
            fail("this method should throw an ex");
        } catch (Exception e) {
            assertEquals("Please check the notify configuration", e.getMessage());
        }
    }

    @Test
    public void testEmailSendWhenEmailsNotifyDisabled() throws Exception {
        when(mockNotifyClientFactory.isEmailNotifyEnabled()).thenReturn(false);
        userNotificationService = new UserNotificationService(mockDirectDebitConfig, mockNotifyClient, mockMetricRegistry, mockGatewayAccountService);

        Future<Optional<String>> idF = userNotificationService.sendMandateFailedEmail(payer, transaction);
        idF.get(1000, TimeUnit.SECONDS);

        verifyZeroInteractions(mockNotifyClient);
    }

    @Test
    public void shouldRecordNotifyResponseTimesWhenSendEmailSucceeds() throws Exception {
        when(mockNotifyClientFactory.isEmailNotifyEnabled()).thenReturn(true);
        when(mockNotifyClient.sendEmail(any(), any(), any(), any())).thenReturn(mockNotificationCreatedResponse);

        userNotificationService = new UserNotificationService(mockDirectDebitConfig, mockNotifyClient, mockMetricRegistry, mockGatewayAccountService);

        Future<Optional<String>> idF = userNotificationService.sendMandateFailedEmail(payer, transaction);
        idF.get(1000, TimeUnit.SECONDS);
        verify(mockMetricRegistry).histogram("notify-operations.response_time");
        verify(mockHistogram).update(anyLong());
        verifyNoMoreInteractions(mockCounter);
    }

    @Test
    public void shouldRecordNotifyResponseTimesAndFailureWhenSendEmailFails() throws Exception {
        when(mockNotifyClientFactory.isEmailNotifyEnabled()).thenReturn(true);
        when(mockNotifyClient.sendEmail(any(), any(), any(), any())).thenThrow(NotificationClientException.class);

        userNotificationService = new UserNotificationService(mockDirectDebitConfig, mockNotifyClient, mockMetricRegistry, mockGatewayAccountService);

        Future<Optional<String>> idF = userNotificationService.sendMandateFailedEmail(payer, transaction);
        idF.get(1000, TimeUnit.SECONDS);
        verify(mockMetricRegistry).histogram("notify-operations.response_time");
        verify(mockHistogram).update(anyLong());
        verify(mockCounter).inc();
    }
}
