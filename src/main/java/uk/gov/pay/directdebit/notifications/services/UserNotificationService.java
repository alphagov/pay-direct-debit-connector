package uk.gov.pay.directdebit.notifications.services;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.services.GatewayAccountService;
import uk.gov.pay.directdebit.notifications.config.NotifyClientFactory;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.lang.Runtime.getRuntime;
import static org.apache.commons.lang3.StringUtils.isBlank;


public class UserNotificationService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(UserNotificationService.class);

    private boolean emailNotifyGloballyEnabled;
    private ExecutorService executorService;
    private final MetricRegistry metricRegistry;
    private final NotificationClient notificationClient;
    private final DirectDebitConfig directDebitConfig;
    private final GatewayAccountService gatewayAccountService;

    @Inject
    public UserNotificationService(DirectDebitConfig directDebitConfig,
                                   NotificationClient notificationClient,
                                   MetricRegistry metricRegistry,
                                   GatewayAccountService gatewayAccountService) {
        this.emailNotifyGloballyEnabled = directDebitConfig.getNotifyConfig().isEmailNotifyEnabled();
        if (emailNotifyGloballyEnabled) {
            int numberOfThreads = directDebitConfig.getExecutorServiceConfig().getThreadsPerCpu() * getRuntime().availableProcessors();
            executorService = Executors.newFixedThreadPool(numberOfThreads);
        }
        this.directDebitConfig = directDebitConfig;
        this.notificationClient = notificationClient;
        this.metricRegistry = metricRegistry;
        this.gatewayAccountService = gatewayAccountService;
    }

    public Future<Optional<String>> sendMandateFailedEmail(Payer payer, Transaction transaction) {
        if (emailNotifyGloballyEnabled) {
            String mandateFailedTemplateId = directDebitConfig.getNotifyConfig().getMandateFailedTemplateId();
            Stopwatch responseTimeStopwatch = Stopwatch.createStarted();
            return executorService.submit(() -> {
                try {
                    SendEmailResponse response = notificationClient
                            .sendEmail(
                                    mandateFailedTemplateId,
                                    payer.getEmail(),
                                    buildMandateFailedPersonalisation(transaction),
                                    null);
                    return Optional.of(response.getNotificationId().toString());
                } catch (NotificationClientException e) {
                    LOGGER.error("Failed to send mandate failed email, payment request id: {}, error {}", transaction.getPaymentRequestExternalId(), e);
                    metricRegistry.counter("notify-operations.failures").inc();
                    return Optional.empty();
                } finally {
                    responseTimeStopwatch.stop();
                    metricRegistry.histogram("notify-operations.response_time").update(responseTimeStopwatch.elapsed(TimeUnit.MILLISECONDS));
                }
            });
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    private HashMap<String, String> buildMandateFailedPersonalisation(Transaction transaction) {
        GatewayAccount gatewayAccount = gatewayAccountService.getGatewayAccountFor(transaction);

        HashMap<String, String> map = new HashMap<>();
        map.put("org name", gatewayAccount.getServiceName());
        map.put("org phone", "000-CAKE-000");
        map.put("dd guarantee link", directDebitConfig.getLinks().getFrontendUrl() + "/direct-debit-guarantee");

        return map;
    }
}
