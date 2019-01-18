package uk.gov.pay.directdebit.mandate.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum MandateType {
    ONE_OFF,
    ON_DEMAND;

    private static final Logger LOGGER = LoggerFactory.getLogger(MandateType.class);

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
