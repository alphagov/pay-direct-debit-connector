package uk.gov.pay.directdebit.notifications.model;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

import java.util.Map;

public class EmailPayload {

    private String address;
    private String gatewayAccountId;
    private EmailTemplate template;
    private Map<String, String> personalisation;

    public String getAddress() {
        return address;
    }

    public String getGatewayAccountId() {
        return gatewayAccountId;
    }

    public EmailTemplate getTemplate() {
        return template;
    }

    public Map<String, String> getPersonalisation() {
        return personalisation;
    }

    public enum EmailTemplate {
        MANDATE_CANCELLED,
        MANDATE_FAILED,
        PAYMENT_CONFIRMED;

        private static final Logger LOGGER = PayLoggerFactory.getLogger(EmailTemplate.class);

        public static EmailTemplate fromString(String type) {
            for (EmailTemplate typeEnum : values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    return typeEnum;
                }
            }
            LOGGER.warn("Unknown email template: {}", type);
            return null;
        }
    }



}
