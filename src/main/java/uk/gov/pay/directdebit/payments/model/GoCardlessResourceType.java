package uk.gov.pay.directdebit.payments.model;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

public enum GoCardlessResourceType {
    PAYMENTS, MANDATES, PAYOUTS, UNHANDLED;
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessResourceType.class);

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
