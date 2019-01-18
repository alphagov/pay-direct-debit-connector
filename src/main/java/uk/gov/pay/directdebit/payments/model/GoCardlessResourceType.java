package uk.gov.pay.directdebit.payments.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum GoCardlessResourceType {
    PAYMENTS, MANDATES, PAYOUTS, UNHANDLED;
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessResourceType.class);

    public static GoCardlessResourceType fromString(String type) {
        for (GoCardlessResourceType typeEnum : GoCardlessResourceType.values()) {
            if (typeEnum.toString().equalsIgnoreCase(type)) {
                return typeEnum;
            }
        }
        LOGGER.warn("Unhandled resource type received in a GoCardless Webhook: {}", type);
        return UNHANDLED;
    }
}
