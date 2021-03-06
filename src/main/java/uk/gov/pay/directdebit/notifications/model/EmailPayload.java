package uk.gov.pay.directdebit.notifications.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EmailPayload {

    private String address;
    private String gatewayAccountExternalId;
    private EmailTemplate template;
    private Map<String, String> personalisation;

    public String getAddress() {
        return address;
    }

    public String getGatewayAccountExternalId() {
        return gatewayAccountExternalId;
    }

    public EmailTemplate getTemplate() {
        return template;
    }

    public Map<String, String> getPersonalisation() {
        return personalisation;
    }

    public enum EmailTemplate {
        ON_DEMAND_MANDATE_CREATED,
        ONE_OFF_MANDATE_CREATED,
        MANDATE_CANCELLED,
        MANDATE_FAILED,
        ONE_OFF_PAYMENT_CONFIRMED,
        ON_DEMAND_PAYMENT_CONFIRMED,
        PAYMENT_FAILED;

        private static final Logger LOGGER = LoggerFactory.getLogger(EmailTemplate.class);

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
