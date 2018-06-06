package uk.gov.pay.directdebit.mandate.model;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

public enum MandateType  {
    ONE_OFF("one off"), 
    ON_DEMAND("on demand");

    private static final Logger LOGGER = PayLoggerFactory.getLogger(MandateType.class);
    private final String readableName;
    MandateType(String readableName) {
        this.readableName = readableName;
    }

    public static MandateType fromString(String type) {
        for (MandateType typeEnum : MandateType.values()) {
            if (typeEnum.toString().equalsIgnoreCase(type) || typeEnum.readableName.equalsIgnoreCase(type)) {
                return typeEnum;
            }
        }
        LOGGER.error("Unexpected mandate type: {}", type);
        return null;
    }
    
}
