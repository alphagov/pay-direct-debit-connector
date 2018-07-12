package uk.gov.pay.directdebit.mandate.model;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

public enum MandateType {
    ONE_OFF,
    ON_DEMAND;

    private static final Logger LOGGER = PayLoggerFactory.getLogger(MandateType.class);

    public static MandateType fromString(String type) {
        for (MandateType typeEnum : values()) {
            if (typeEnum.toString().equalsIgnoreCase(type)) {
                return typeEnum;
            }
        }
        LOGGER.error("Invalid mandate type: {}", type);
        return null;
    }
}
