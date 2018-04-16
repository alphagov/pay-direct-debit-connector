package uk.gov.pay.directdebit.notifications.services;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.services.GatewayAccountService;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.lang.Runtime.getRuntime;


public class UserNotificationService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(UserNotificationService.class);
    private static final String PLACEHOLDER_PHONE_NUMBER = "+44 000-CAKE-000";
    private static final String PLACEHOLDER_SUN = "THE-CAKE-IS-A-LIE";
    private static final String PLACEHOLDER_MERCHANT_ADDRESS = "123 Rainbow Road, EC125Y, London";

    private static final String DD_GUARANTEE_KEY = "dd guarantee link";
    private static final String MANDATE_REFERENCE_KEY = "mandate reference";
    private static final String ORG_NAME_KEY = "org name";
    private static final String SERVICE_NAME_KEY = "service name";
    private static final String SUN_KEY = "SUN";
    private static final String COLLECTION_DATE_KEY = "collection date";
    private static final String AMOUNT_KEY = "amount";
    private static final String PAYMENT_REFERENCE_KEY = "payment reference";
    private static final String MERCHANT_ADDRESS_KEY = "merchant address";
    private static final String ORG_PHONE_KEY = "org phone";
    private static final String MERCHANT_PHONE_NUMBER_KEY = "merchant phone number";
    private static final String BANK_ACCOUNT_LAST_DIGITS_KEY = "bank account last 2 digits";

    private boolean emailNotifyGloballyEnabled;
    private ExecutorService executorService;
    private final MetricRegistry metricRegistry;
    private final NotificationClient notificationClient;
    private final DirectDebitConfig directDebitConfig;
    private final GatewayAccountService gatewayAccountService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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

    public Future<Optional<String>> sendEmail(Map<String, String> personalisation,
                                              String templateId,
                                              String emailAddress,
                                              String paymentRequestExternalId) {
        if (emailNotifyGloballyEnabled) {
            Stopwatch responseTimeStopwatch = Stopwatch.createStarted();
            return executorService.submit(() -> {
                try {
                    SendEmailResponse response = notificationClient
                            .sendEmail(
                                    templateId,
                                    emailAddress,
                                    personalisation,
                                    null);
                    return Optional.of(response.getNotificationId().toString());
                } catch (NotificationClientException e) {
                    LOGGER.error("Failed to send email, payment request id: {}, error from notify {}", paymentRequestExternalId, e);
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

    public Future<Optional<String>> sendMandateFailedEmailFor(Transaction transaction, Payer payer) {
        String mandateFailedTemplateId = directDebitConfig.getNotifyConfig().getMandateFailedTemplateId();
        LOGGER.info("Sending mandate failed email, payment request id: {}, gateway account id: {}",
                transaction.getPaymentRequestExternalId(),
                transaction.getGatewayAccountExternalId());
        return sendEmail(buildMandateFailedPersonalisation(transaction),
                mandateFailedTemplateId,
                payer.getEmail(),
                transaction.getPaymentRequestExternalId());
    }
    public Future<Optional<String>> sendMandateCancelledEmailFor(Transaction transaction, Mandate mandate, Payer payer) {
        String mandateCancelledTemplateId = directDebitConfig.getNotifyConfig().getMandateCancelledTemplateId();
        LOGGER.info("Sending mandate cancelled email, payment request id: {}, gateway account id: {}",
                transaction.getPaymentRequestExternalId(),
                transaction.getGatewayAccountExternalId());
        return sendEmail(buildMandateCancelledPersonalisation(transaction, mandate),
                mandateCancelledTemplateId,
                payer.getEmail(),
                transaction.getPaymentRequestExternalId());
    }
    public Future<Optional<String>> sendPaymentConfirmedEmailFor(Transaction transaction, Payer payer, LocalDate earliestChargeDate) {
        String paymentConfirmedTemplateId = directDebitConfig.getNotifyConfig().getPaymentConfirmedTemplateId();
        LOGGER.info("Sending payment confirmed email, payment request id: {}, gateway account id: {}",
                transaction.getPaymentRequestExternalId(),
                transaction.getGatewayAccountExternalId());
        return sendEmail(buildPaymentConfirmedPersonalisation(transaction, payer,earliestChargeDate),
                paymentConfirmedTemplateId,
                payer.getEmail(),
                transaction.getPaymentRequestExternalId());
    }

    private HashMap<String, String> buildMandateFailedPersonalisation(Transaction transaction) {
        GatewayAccount gatewayAccount = gatewayAccountService.getGatewayAccountFor(transaction);

        HashMap<String, String> map = new HashMap<>();
        map.put(ORG_NAME_KEY, gatewayAccount.getServiceName());
        map.put(ORG_PHONE_KEY, PLACEHOLDER_PHONE_NUMBER);
        map.put(DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl());

        return map;
    }

    private HashMap<String, String> buildMandateCancelledPersonalisation(Transaction transaction, Mandate mandate) {
        GatewayAccount gatewayAccount = gatewayAccountService.getGatewayAccountFor(transaction);

        HashMap<String, String> map = new HashMap<>();
        // fixme use the right reference once we play the story
        map.put(MANDATE_REFERENCE_KEY, mandate.getReference());
        map.put(ORG_NAME_KEY, gatewayAccount.getServiceName());
        map.put(ORG_PHONE_KEY, PLACEHOLDER_PHONE_NUMBER);
        map.put(DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl());

        return map;
    }

    private HashMap<String, String> buildPaymentConfirmedPersonalisation(Transaction transaction, Payer payer, LocalDate earliestChargeDate) {
        GatewayAccount gatewayAccount = gatewayAccountService.getGatewayAccountFor(transaction);

        HashMap<String, String> map = new HashMap<>();
        map.put(SERVICE_NAME_KEY, gatewayAccount.getServiceName());
        map.put(AMOUNT_KEY, formatToPounds(transaction.getAmount()));
        map.put(PAYMENT_REFERENCE_KEY, transaction.getPaymentRequestReference());
        map.put(BANK_ACCOUNT_LAST_DIGITS_KEY, "******" + payer.getAccountNumberLastTwoDigits());
        map.put(COLLECTION_DATE_KEY, DATE_TIME_FORMATTER.format(earliestChargeDate));
        map.put(SUN_KEY, PLACEHOLDER_SUN);
        map.put(MERCHANT_ADDRESS_KEY, PLACEHOLDER_MERCHANT_ADDRESS);
        map.put(MERCHANT_PHONE_NUMBER_KEY, PLACEHOLDER_PHONE_NUMBER);
        map.put(DD_GUARANTEE_KEY, directDebitConfig.getLinks().getDirectDebitGuaranteeUrl());
        return map;
    }

    private String formatToPounds(long amountInPence) {
        return BigDecimal.valueOf(amountInPence, 2).toString();
    }

}
