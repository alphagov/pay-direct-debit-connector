package uk.gov.pay.directdebit.notifications.config;

import io.dropwizard.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.service.notify.NotificationClient;

import javax.validation.constraints.NotNull;

import static uk.gov.pay.directdebit.app.ssl.TrustStoreLoader.getSSLContext;

public class NotifyClientFactory extends Configuration {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(NotifyClientFactory.class);

    @NotNull
    private String apiKey;
    @NotNull
    private String notificationBaseURL;
    @NotNull
    private boolean emailNotifyEnabled;
    @NotNull
    private String mandateFailedTemplateId;

    public String getMandateFailedTemplateId() {
        return mandateFailedTemplateId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getNotificationBaseURL() {
        return notificationBaseURL;
    }

    public boolean isEmailNotifyEnabled() {
        if (!emailNotifyEnabled) {
            LOGGER.warn("Email notifications are globally disabled by configuration");
        }
        return emailNotifyEnabled;
    }

    public NotificationClient getInstance() {
        return newInstance(getApiKey(), getNotificationBaseURL());
    }

    private NotificationClient newInstance(String apiKey, String notificationBaseURL) {
        return new NotificationClient(apiKey, notificationBaseURL, null, getSSLContext());
    }
}
