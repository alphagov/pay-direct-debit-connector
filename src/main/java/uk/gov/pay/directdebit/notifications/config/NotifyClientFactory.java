package uk.gov.pay.directdebit.notifications.config;

import io.dropwizard.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.service.notify.NotificationClient;

import static uk.gov.pay.directdebit.app.ssl.TrustStoreLoader.getSSLContext;

public class NotifyClientFactory extends Configuration {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private String mandateFailedTemplateId;
    private String apiKey;
    private String notificationBaseURL;
    private boolean emailNotifyEnabled;

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
        return emailNotifyEnabled;
    }

    public NotificationClient getInstance() {
        return newInstance(getApiKey(), getNotificationBaseURL());
    }

    private NotificationClient newInstance(String apiKey, String notificationBaseURL) {
        return new NotificationClient(apiKey, notificationBaseURL, null, getSSLContext());
    }
}
