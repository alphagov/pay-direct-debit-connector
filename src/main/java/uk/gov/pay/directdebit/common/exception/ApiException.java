package uk.gov.pay.directdebit.common.exception;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;

public class ApiException extends RuntimeException {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(ApiException.class);
    private Integer statusCode;

    Integer getStatusCode() {
        return statusCode;
    }

    ApiException(Integer statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        LOGGER.error(message);
    }
}
