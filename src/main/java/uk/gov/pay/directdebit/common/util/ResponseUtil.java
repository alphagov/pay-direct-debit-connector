package uk.gov.pay.directdebit.common.util;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.List;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.*;
import static javax.ws.rs.core.Response.Status.*;

public class ResponseUtil {
    protected static final Logger logger = LoggerFactory.getLogger(ResponseUtil.class);
    private static final Joiner COMMA_JOINER = Joiner.on(", ");

    public static Response fieldsMissingResponse(List<String> missingFields) {
        String message = format("Field(s) missing: [%s]", COMMA_JOINER.join(missingFields));
        return badRequestResponse(message);
    }

    public static Response fieldsInvalidSizeResponse(List<String> invalidSizeFields) {
        String message = format("Field(s) are too big: [%s]", COMMA_JOINER.join(invalidSizeFields));
        return badRequestResponse(message);
    }

    public static Response fieldsInvalidResponse(List<String> invalidFields) {
        String message = format("Field(s) are invalid: [%s]", COMMA_JOINER.join(invalidFields));
        return badRequestResponse(message);
    }

    public static Response responseWithChargeNotFound(String chargeId) {
        String message = format("Charge with id [%s] not found.", chargeId);
        return notFoundResponse(message);
    }

    public static Response responseWithRefundNotFound(String refundId) {
        String message = format("Refund with id [%s] not found.", refundId);
        return notFoundResponse(message);
    }

    public static Response badRequestResponse(String message) {
        logger.error(message);
        return responseWithMessageMap(BAD_REQUEST, message);
    }

    public static Response badRequestResponse(String code, String message) {
        logger.error(message);
        return responseWith(BAD_REQUEST, message, code);
    }

    public static Response badRequestResponse(List message) {
        logger.error(message.toString());
        return responseWithMessageMap(BAD_REQUEST, message);
    }

    public static Response preconditionFailedResponse(String message) {
        logger.info(message.toString());
        return responseWithMessageMap(PRECONDITION_FAILED, message);
    }

    public static Response notFoundResponse(String message) {
        logger.error(message);
        return responseWithMessageMap(NOT_FOUND, message);
    }

    public static Response acceptedResponse(String message) {
        logger.error(message);
        return responseWithMessageMap(ACCEPTED, message);
    }

    public static Response serviceErrorResponse(String message) {
        logger.error(message);
        return responseWithMessageMap(INTERNAL_SERVER_ERROR, message);
    }

    public static Response conflictErrorResponse(String message) {
        logger.error(message);
        return responseWithMessageMap(CONFLICT, message);
    }

    public static Response forbiddenErrorResponse() {
        return status(Status.FORBIDDEN).build();
    }

    private static Response responseWithMessageMap(Status status, String message) {
        return responseWithEntity(status, ImmutableMap.of("message", message));
    }

    private static Response responseWith(Status status, String message, String reason) {
        return responseWithEntity(status, ImmutableMap.of("reason", reason, "message", message));
    }

    private static Response responseWithMessageMap(Status status, Object entity) {
        return responseWithEntity(status, ImmutableMap.of("message", entity));
    }

    public static Response noContentResponse() {
        return noContent().build();
    }

    public static Response successResponseWithEntity(Object entity) {
        return responseWithEntity(OK, entity);
    }

    private static Response responseWithEntity(Status status, Object entity) {
        return status(status).entity(entity).build();
    }
}
